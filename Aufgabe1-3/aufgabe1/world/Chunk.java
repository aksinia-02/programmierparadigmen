package aufgabe1.world;

import aufgabe1.IVector;
import aufgabe1.Vector;
import aufgabe1.world.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Chunk class represents a chunk of the world, containing multiple cells. It manages the
 * cells and provides methods for accessing and interacting with them.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class Chunk implements Iterable<Cell>, ChunkView {

    private static final int CHUNK_SIZE_SHIFT = 5;
    public static final int CHUNK_SIZE = 1 << CHUNK_SIZE_SHIFT;
    private static final IVector SIZE = new Vector(CHUNK_SIZE, CHUNK_SIZE);
    @NotNull
    final ReentrantLock mutex = new ReentrantLock();
    @NotNull
    private final Cell @NotNull [] cells;
    @NotNull
    private final SuspendState @NotNull [] states;
    private final int chunkX;
    private final int chunkY;
    @NotNull
    private final IVector origin;
    @NotNull
    private final IVector limit;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @NotNull
    private final World world;
    @NotNull
    private final WorldGenerator generator;
    @NotNull
    private final CellCache cache = new CellCache();
    @NotNull
    private final Set<Entity> entities = ConcurrentHashMap.newKeySet();
    @Nullable
    private Synchronizer.Worker owner;
    @NotNull
    private SuspendState state = SuspendState.AWAKE;
    private boolean populated = false;
    private boolean hasCompleteNeighbors = false;

    public Chunk(@NotNull World world, @NotNull WorldGenerator generator, int chunkX, int chunkY) {
        this.world = world;
        this.generator = generator;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.origin = new Vector(chunkX << CHUNK_SIZE_SHIFT, chunkY << CHUNK_SIZE_SHIFT);
        this.limit = new Vector(this.origin).add(Chunk.SIZE);
        this.cells = new Cell[CHUNK_SIZE * CHUNK_SIZE];
        this.states = new SuspendState[CHUNK_SIZE * CHUNK_SIZE];
        for (int dx = 0; dx < CHUNK_SIZE; dx++) {
            for (int dy = 0; dy < CHUNK_SIZE; dy++) {
                int index = dx + dy * CHUNK_SIZE;
                cells[index] = new Cell(world, new Vector(origin).add(dx, dy));
                states[index] = SuspendState.AWAKE;
            }
        }
    }

    /**
     * Converts a cell X-coordinate to a chunk X-coordinate.
     */
    public static int toChunkX(int cellX) {
        return cellX >> CHUNK_SIZE_SHIFT;
    }

    /**
     * Converts a cell Y-coordinate to a chunk Y-coordinate.
     */
    public static int toChunkY(int cellY) {
        return cellY >> CHUNK_SIZE_SHIFT;
    }

    /**
     * Populates the chunk with generated content.
     */
    public void populate() {
        if (populated) throw new IllegalStateException("Chunk is already populated");
        populated = true;
        for (int dx = 0; dx < CHUNK_SIZE; dx++) {
            for (int dy = 0; dy < CHUNK_SIZE; dy++) {
                int index = dx + dy * CHUNK_SIZE;
                Cell cell = cells[index];
                generator.generate(cell);
            }
        }
    }

    public boolean populated() {
        return populated;
    }

    public boolean hasCompleteNeighbors() {
        return this.hasCompleteNeighbors;
    }

    public void setHasCompleteNeighbors(boolean hasCompleteNeighbors) {
        this.hasCompleteNeighbors = hasCompleteNeighbors;
    }

    /**
     * Retrieves a cell in the chunk based on its position.
     */
    @NotNull
    public Cell get(int x, int y) {
        if (cache.matches(x, y)) return cache.get();

        if (!isInside(x, y)) {
            throw new RuntimeException(String.format("Position %d,%d is not inside chunk %s", x, y, this));
        }
        int dx = x - origin.x();
        int dy = y - origin.y();
        int index = dx + dy * CHUNK_SIZE;
        Cell cell = cells[index];

        cache.set(cell, index);
        return cell;
    }

    /**
     * Checks if a given position is inside the chunk's boundaries.
     */
    private boolean isInside(int x, int y) {
        return x >= origin.x() && y >= origin.y() && x < limit.x() && y < limit.y();
    }

    /**
     * Awakens a cell at the specified position.
     */
    synchronized void awake(int x, int y) {
        int dx = x - origin.x();
        int dy = y - origin.y();
        int index = dx + dy * CHUNK_SIZE;
        states[index] = SuspendState.AWAKE;
        state = SuspendState.AWAKE;
    }

    /**
     * Suspends a cell at the specified position.
     */
    synchronized void suspend(int x, int y) {
        int dx = x - origin.x();
        int dy = y - origin.y();
        int index = dx + dy * CHUNK_SIZE;
        states[index] = SuspendState.SUSPENDED;
    }

    @Contract(pure = true)
    @NotNull
    public SuspendState suspendState() {
        return state;
    }

    /**
     * Suspends the entire chunk
     */
    void suspend() {
        this.state = SuspendState.SUSPENDED;
    }


    @Override
    @NotNull
    public IVector origin() {
        return origin;
    }

    @NotNull
    public IVector limit() {
        return limit;
    }

    @Override
    @NotNull
    public IVector size() {
        return SIZE;
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkY() {
        return chunkY;
    }

    @Override
    @NotNull
    public String toString() {
        return "Chunk{" + chunkX + ", " + chunkY + '}';
    }

    Cell @NotNull [] cells() {
        return cells;
    }

    Synchronizer.@Nullable Worker getOwner() {
        return owner;
    }

    void setOwner(Synchronizer.@Nullable Worker owner) {
        this.owner = owner;
    }

    SuspendState @NotNull [] cellStates() {
        return states;
    }

    @NotNull
    @Override
    public java.util.Iterator<@Nullable Cell> iterator() {
        return new Iterator(cells);
    }

    public void track(Entity entity) {
        entities.add(entity);
    }

    @NotNull
    @UnmodifiableView
    public Set<Entity> entities() {
        return Collections.unmodifiableSet(entities);
    }

    public void untrack(Entity entity) {
        entities.remove(entity);
    }

    private static class Iterator implements java.util.Iterator<Cell> {
        private final Cell[] cells;
        private int index = 0;

        private Iterator(Cell[] cells) {
            this.cells = cells;
        }

        @Override
        public boolean hasNext() {
            return index < cells.length;
        }

        @Override
        public Cell next() {
            return cells[index++];
        }
    }

    private static class CellCache {
        private Cell value;
        private int x;
        private int y;
        private int index;

        public boolean matches(int x, int y) {
            return value != null && this.x == x && this.y == y;
        }

        public void set(@NotNull Cell cell, int index) {
            this.value = cell;
            this.x = cell.position().x();
            this.y = cell.position().y();
            this.index = index;
        }

        @SuppressWarnings("unused")
        public void set(@NotNull CellCache cache) {
            this.value = cache.value;
            this.x = cache.x;
            this.y = cache.y;
            this.index = cache.index;
        }

        public Cell get() {
            return value;
        }

        @SuppressWarnings("unused")
        public int index() {
            return index;
        }
    }

    public boolean isTracking(Entity entity) {
        return entities.contains(entity);
    }
}
