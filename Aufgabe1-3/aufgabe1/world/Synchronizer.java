package aufgabe1.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented and parallel
 */
public class Synchronizer implements ThreadFactory {
    private final ThreadLocal<Worker> localWorker = ThreadLocal.withInitial(Worker::new);

    private final ChunkMap chunks;
    private final AtomicInteger workerId = new AtomicInteger();
    private final ThreadGroup group = new ThreadGroup("chunk-updaters");
    private final AtomicBoolean active = new AtomicBoolean(false);

    public Synchronizer(ChunkMap chunks) {
        this.chunks = chunks;
    }

    @Nullable
    public Chunk getAndLockOrNull(int chunkX, int chunkY) {
        Chunk chunk = chunks.getOrNull(chunkX, chunkY);
        if (chunk == null) return null;
        lock(chunk);
        return chunk;
    }


    // STYLE: Parallel
    // See code comments within this method.
    // When a worker thread begins a task it locks the initial chunk
    // using lockFirst. Any other chunks that it accesses are locked in here.
    // Only when the worker finished processing its task it is safe to call releaseAll
    // since only then it can be (quite) sure that no update code is holding onto a Chunk reference.
    // This implies that during a task a thread will only lock any chunk once and will only release
    // the locks when the task is done.

    /**
     * Locks the given chunk immediately or when it becomes available.
     * The current worker will be the chunk's owner when the method returns.
     * The synchronizer should be active before calling this method.
     */
    public void lock(@NotNull Chunk chunk) {
        // localWorker hold a reference to this thread's associated worker instance
        Worker worker = localWorker.get();
        while (true) {
            synchronized (chunk.mutex) {
                // Lock the chunk if it is available
                if (chunk.mutex.tryLock()) {
                    // Clear 'wait' dependency, if any and declare the ownership
                    worker.waits = null;
                    chunk.setOwner(worker);
                    worker.locks.add(chunk);
                    break;
                }
                // The Chunk is not available, so it is declared as a 'wait' dependency
                worker.waits = chunk.getOwner();

                // A deadlock condition arises when the graph formed
                // by the waiting dependencies is cyclic
                if (worker.isCyclicWait()) {
                    // Calling wait now would deadlock
                    // but since this worker is the 'last' link in a cycle it is 'relatively safe'
                    // to access the chunk anyway since chunk's over is effectively waiting
                    // on the current worker.
                    //  ┌───────┐  Waits  ┌────────┐ | In this example 'this' is 'Worker A' and 'chunk' is 'Chunk Y'
                    //  │Chunk X│◄────────┤Worker B│ | B wants to access Y but Y is already owned by A. So B is waiting
                    //  └───┬───┘         └───▲────┘ | for A to release X.
                    //      │Owner            │      | If A now also waits for Y a deadlock arises. So instead, since
                    //      │                 │Owner | Y is owned by B and B is currently waiting on A, it is
                    // ┌────▼───┐         ┌───┴───┐  | 'relatively safe' for A to operate on Y, as no other worker
                    // │Worker A├────────►│Chunk Y│  | can access it at this time. This avoids the deadlock but may
                    // └────────┘  Waits  └───────┘  | lead to undefined behavior in some cases.
                    //
                    System.err.println("Deadlock condition occurred, undefined behavior will result");
                    break;
                }

                try {
                    // Wait for the chunk to become available.
                    // This has to be done in a loop since it is possible for the lock
                    // to be 'stolen' in the time between wait() and tryLock()
                    // NOTE: this might not be true, as it's inside a synchronized block
                    chunk.mutex.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * The synchronizer should be active before calling this method.
     */
    @NotNull
    public Chunk getAndLock(int chunkX, int chunkY) {
        Chunk chunk = chunks.get(chunkX, chunkY);
        lock(chunk);
        return chunk;
    }

    /**
     * This method is similar to {@link #lock}, except it must not be used if the worker
     * owns any chunks.
     * The synchronizer should be active before calling this method.
     */
    public void lockFirst(@NotNull Chunk chunk) {
        Worker worker = localWorker.get();

        if (!worker.locks.isEmpty()) {
            throw new IllegalStateException("Worker has unreleased chunks");
        }

        while (true) {
            synchronized (chunk.mutex) {
                if (chunk.mutex.tryLock()) {
                    worker.waits = null;
                    if (chunk.getOwner() == null) {
                        chunk.setOwner(worker);
                        worker.locks.add(chunk);
                    } else if (chunk.getOwner() != worker) {
                        throw new IllegalStateException("Chunk unlocked but owner set");
                    }
                    break;
                }
                if (chunk.getOwner() != worker) {
                    throw new IllegalStateException("Chunk locked but owner null");
                }
                worker.waits = chunk.getOwner();
                try {
                    // can't deadlock since worker doesn't own any chunks currently
                    chunk.mutex.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Releases all owned chunks for this thread's worker.
     */
    public void releaseAll() {
        Worker worker = localWorker.get();
        for (Chunk chunk : worker.locks) {
            synchronized (chunk.mutex) {
                chunk.setOwner(null);
                chunk.mutex.unlock();
                chunk.mutex.notifyAll();
            }
        }
        worker.locks.clear();
    }

    @Override
    @NotNull
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(group, r, String.format("worker-%d", workerId.getAndIncrement()));
        thread.setPriority(Thread.MAX_PRIORITY);
        return thread;
    }

    /**
     * @return true if the current thread is a worker thread
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean onWorkerThread() {
        return Thread.currentThread().getThreadGroup() == group;
    }

    public synchronized void begin() {
        active.set(true);
    }

    public synchronized void end() {
        active.set(false);
        this.notifyAll();
    }

    /**
     * @see #waitUntilDone(long)
     */
    public void waitUntilDone() {
        waitUntilDone(0);
    }

    /**
     * If the synchronizer is active it waits until {@link #end()} is called
     */
    public synchronized void waitUntilDone(long timeoutMs) {
        if (!active.get()) return;

        try {
            wait(timeoutMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class Worker {
        private final List<Chunk> locks = new ArrayList<>();
        private @Nullable Worker waits = null;

        public boolean isCyclicWait() {
            Worker current = this;
            while (current.waits != null) {
                if (current.waits == this) return true;
                current = current.waits;
            }
            return false;
        }
    }
}
