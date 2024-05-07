package aufgabe1.world;

import aufgabe1.IVector;
import aufgabe1.Simulation;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.Colony;
import aufgabe1.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.StreamSupport;

/**
 * The world represents a wrapping 2D grid manages all elements of a simulation.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class World {
    private static final boolean DEBUG_ASYNC_CHECKS = false;
    private static final boolean DEBUG_SINGLE_THREADED = false;
    private final UUID uuid = UUID.randomUUID();
    @NotNull
    private final ChunkMap chunks;
    @NotNull
    private final WorldGenerator generator;
    // There are few colonies so a list is faster than a set probably
    @NotNull
    private final List<Colony> colonies = new ArrayList<>();
    @NotNull
    private final ExecutorService executor;
    @NotNull
    private final Synchronizer synchronizer;
    private final ReentrantLock lock = new ReentrantLock(true);
    @Nullable
    private Simulation simulation;
    @NotNull
    private WorldParameters parameters;
    private int time;

    public World(@NotNull WorldGenerator generator, @NotNull WorldParameters parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
        this.chunks = new ChunkMap(this, generator, 7);
        this.generator = generator;
        this.synchronizer = new Synchronizer(this.chunks);
        if (DEBUG_SINGLE_THREADED) {
            this.executor = Executors.newSingleThreadExecutor(synchronizer);
        } else {
            int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
            this.executor = Executors.newFixedThreadPool(threads, synchronizer);
        }
    }

    /**
     * Gracefully shuts down the thread pool
     */
    public void close() {
        synchronizer.waitUntilDone();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Executor did not shut down within 5 seconds");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.close();
    }

    /**
     * Updates the world by processing ants, colonies and cells.
     */
    public void update() {
        // entities that moved into another chunk
        List<Map.Entry<Entity, Chunk>> trackInvalid = new ArrayList<>();

        // STYLE: Parallel
        // Chunks are updated in parallel for a major performance boost
        // They are split up into 9 batches to reduce interference and stalling.
        // A deadlock condition detection is used to prevent deadlocks at the
        // cost of having a low chance for undefined behavior.
        // The Synchronizer class is responsible for managing thread chunk access.
        // Chunks are locked "automagically" when they are accessed by a thread to avoid
        // the need to change code elsewhere.
        // If a foreign (non-worker) thread accesses the world while it's being updated it is forced to wait
        // until the update is finished. The world can be locked for exclusive access to the chunks. This will
        // delay the next update until the world is unlocked again.
        // More info in can be found in the Visualization class
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        lock.lock();
        synchronizer.begin();
        for (List<Chunk> batch : chunks.batches()) {
            try {
                // work completion counter
                AtomicInteger todo = new AtomicInteger(batch.size());

                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < batch.size(); i++) {
                    final Chunk chunk = batch.get(i);
                    executor.execute(() -> {
                        try {
                            synchronizer.lockFirst(chunk);
                            for (Entity entity : chunk.entities()) {
                                int chunkXBefore = Chunk.toChunkX(entity.position().x());
                                int chunkYBefore = Chunk.toChunkY(entity.position().y());
                                //GOOD: Dynamic binding: The actual implementation is chosen at runtime
                                entity.update();
                                int chunkXAfter = Chunk.toChunkX(entity.position().x());
                                int chunkYAfter = Chunk.toChunkY(entity.position().y());
                                boolean moved = chunkXBefore != chunkXAfter || chunkYBefore != chunkYAfter;
                                if (moved && chunk.isTracking(entity)) {
                                    synchronized (trackInvalid) {
                                        trackInvalid.add(Map.entry(entity, chunk));
                                    }
                                }
                            }

                            // Suspended chunks don't need to update their cells and can be skipped
                            if (chunk.suspendState() == SuspendState.AWAKE) {
                                Cell[] cells = chunk.cells();
                                SuspendState[] states = chunk.cellStates();
                                boolean noUpdates = true;
                                for (int j = 0; j < cells.length; j++) {
                                    if (states[j] == SuspendState.SUSPENDED) continue;

                                    Cell cell = cells[j];
                                    cell.update();
                                    noUpdates = false;
                                }
                                if (noUpdates) {
                                    chunk.suspend();
                                }
                            }
                        } finally {
                            synchronizer.releaseAll();
                            synchronized (todo) {
                                if (todo.decrementAndGet() == 0) {
                                    // notify other threads that the work is complete
                                    todo.notifyAll();
                                }
                            }
                        }
                    });
                }

                synchronized (todo) {
                    if (todo.get() > 0) {
                        // wait until all work has been completed
                        todo.wait();
                    }
                }
            } catch (RejectedExecutionException e) {
                // executor has been shut down
                break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (DEBUG_ASYNC_CHECKS) {
                for (Chunk chunk : chunks) {
                    if (chunk == null) continue;
                    if (chunk.getOwner() != null || chunk.mutex.isLocked()) {
                        throw new IllegalStateException("Chunk was not unlocked");
                    }
                }
            }

        }
        synchronizer.end();
        lock.unlock();

        // FIXME: At small chunk sizes ConcurrentModificationException occurs
        // Track entities in their new chunks
        for (Map.Entry<Entity, Chunk> entry : trackInvalid) {
            Entity entity = entry.getKey();
            entry.getValue().untrack(entity);
            Chunk next = chunks.getOrNull(Chunk.toChunkX(entity.position().x()), Chunk.toChunkY(entity.position().y()));
            if (next == null) throw new IllegalStateException("Chunk was null");
            next.track(entity);
        }

        this.generator.setLocked(true);
        for (Chunk chunk : chunks) {
            if (chunk == null || !chunk.populated()) continue;
            this.generator.update(chunk, time);
        }
        this.generator.setLocked(false);

        time++;
    }

    public int time() {
        return time;
    }

    public boolean isNight() {
        return !isDay();
    }

    public boolean isDay() {
        float timeOfDay = (float) (time % parameters.dayNightCycleTime) / parameters.dayNightCycleTime;
        return timeOfDay <= parameters.dayPercentage;
    }

    @NotNull
    public Cell get(@NotNull IVector pos) {
        return get(pos.x(), pos.y());
    }

    /**
     * Gets the cell at the specified coordinates with wrap-around.
     */
    @NotNull
    public Cell get(int x, int y) {
        int chunkX = Chunk.toChunkX(x);
        int chunkY = Chunk.toChunkY(y);
        Chunk chunk = getInternal(chunkX, chunkY);
        return chunk.get(x, y);
    }

    @NotNull
    private Chunk getInternal(int chunkX, int chunkY) {
        if (synchronizer.onWorkerThread()) {
            return synchronizer.getAndLock(chunkX, chunkY);
        }
        synchronizer.waitUntilDone();
        return chunks.get(chunkX, chunkY);
    }

    @Nullable
    public Chunk getChunkOrNull(int chunkX, int chunkY) {
        return getOrNullInternal(chunkX, chunkY);
    }

    @Nullable
    private Chunk getOrNullInternal(int chunkX, int chunkY) {
        if (synchronizer.onWorkerThread()) {
            return synchronizer.getAndLockOrNull(chunkX, chunkY);
        }
        synchronizer.waitUntilDone();
        return chunks.getOrNull(chunkX, chunkY);
    }

    @Nullable
    public Cell getOrNull(int x, int y) {
        int chunkX = Chunk.toChunkX(x);
        int chunkY = Chunk.toChunkY(y);
        Chunk chunk = getOrNullInternal(chunkX, chunkY);
        if (chunk == null) return null;

        return chunk.get(x, y);
    }

    public void track(@NotNull Entity entity) {
        Chunk chunk = getInternal(Chunk.toChunkX(entity.position().x()), Chunk.toChunkY(entity.position().y()));
        chunk.track(entity);

        if (entity instanceof Colony colony && !colonies.contains(colony)) {
            colonies.add(colony);
        }
    }

    public void untrack(@NotNull Entity entity) {
        Chunk chunk = getOrNullInternal(Chunk.toChunkX(entity.position().x()), Chunk.toChunkY(entity.position().y()));
        if (chunk != null) {
            chunk.untrack(entity);
        }
        if (entity instanceof Colony colony) {
            colonies.remove(colony);
        }
    }

    public void setSimulation(@Nullable Simulation simulation) {
        this.simulation = simulation;
        if (simulation != null) {
            this.chunks.setSeed(simulation.randoms().places().nextInt());
        } else {
            this.chunks.setSeed(0);
        }
    }

    @NotNull
    public Optional<Simulation> simulation() {
        return Optional.ofNullable(simulation);
    }

    @NotNull
    public WorldParameters parameters() {
        return parameters;
    }

    @SuppressWarnings("unused")
    public void setParameters(@NotNull WorldParameters parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
    }

    /**
     * Moves an ant from this cell to another cell
     */
    public void moveAnt(@NotNull Ant ant, @NotNull Cell to) {
        if (ant.cell() == to) return;
        ant.cell().removeAnt(ant);
        to.addAnt(ant);
        ant.setCell(to);
    }

    @NotNull
    public UUID uuid() {
        return uuid;
    }

    //STYLE: functional
    public float totalColonyFood() {
        return StreamSupport.stream(chunks.spliterator(), false).filter(Objects::nonNull)
            .flatMap(chunk -> chunk.entities().stream()).filter(Colony.class::isInstance)
            .map(Colony.class::cast)
            .map(Colony::food).reduce(0f, Float::sum);
    }

    //STYLE: functional
    public int totalAnts() {
        return StreamSupport.stream(chunks.spliterator(), false).filter(Objects::nonNull)
            .flatMap(chunk -> chunk.entities().stream()).filter(Colony.class::isInstance)
            .map(Colony.class::cast)
            .map(Colony::ants).map(Set::size)
            .reduce(0, Integer::sum);
    }


    @NotNull
    public WorldGenerator generator() {
        return generator;
    }

    @NotNull
    @UnmodifiableView
    public List<Colony> colonies() {
        return Collections.unmodifiableList(colonies);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean lock(long timeoutMs) {
        synchronizer.waitUntilDone(timeoutMs);
        try {
            return lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void unlock() {
        lock.unlock();
    }

    public void wake(int x, int y) {
        int chunkX = Chunk.toChunkX(x);
        int chunkY = Chunk.toChunkY(y);
        Chunk chunk = getOrNullInternal(chunkX, chunkY);
        if (chunk == null) throw new IllegalStateException("Chunk was null");
        chunk.awake(x, y);
    }

    public void suspend(int x, int y) {
        int chunkX = Chunk.toChunkX(x);
        int chunkY = Chunk.toChunkY(y);
        Chunk chunk = getOrNullInternal(chunkX, chunkY);
        if (chunk == null) throw new IllegalStateException("Chunk was null");
        chunk.suspend(x, y);
    }
}
