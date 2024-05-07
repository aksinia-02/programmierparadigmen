package aufgabe9;

import aufgabe9.arena.Ant;
import aufgabe9.arena.Cell;
import aufgabe9.nest.Leaf;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class World {

    private final int height, width;
    private final Cell[][] world;
    private final Cell nestLowerCorner;
    private final ObjectOutputStream objectOutputStream;
    private final AtomicBoolean freezeAll = new AtomicBoolean(false);
    private final AtomicInteger lockCount = new AtomicInteger();


    public World(int width, int height, ObjectOutputStream objectOutputStream) {
        this.height = height;
        this.width = width;
        this.objectOutputStream = objectOutputStream;
        world = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = new Cell(x, y);
            }
        }
        nestLowerCorner = cell(width / 2, height / 2);
    }

    /**
     * Retrieves the cell at a specific coordinate.
     * Returns null if the cell is out of bounds.
     */
    public Cell cell(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            return null;
        return world[x][y];
    }

    /**
     * Returns the width of the world.
     */
    public int width() {
        return width;
    }

    /**
     * Returns the height of the world.
     */
    public int height() {
        return height;
    }

    /**
     * Determines possible fields for the next head position of an ant based on its current position.
     *
     * @param ant must not be null.
     * @return List of possible cells for the next head position
     */
    public List<Cell> possibleFieldsForNextHead(Ant ant) {
        int x = ant.head().x();
        int y = ant.head().y();
        List<Cell> possibleFields = new ArrayList<>();
        switch (direction(ant)) {
            case UP ->
                    possibleFields.addAll(Arrays.asList(cell(x - 2, y), cell(x - 1, y - 1), cell(x, y - 2), cell(x + 1, y - 1), cell(x + 2, y)));
            case RIGHT ->
                    possibleFields.addAll(Arrays.asList(cell(x, y - 2), cell(x + 1, y - 1), cell(x + 2, y), cell(x + 1, y + 1), cell(x, y + 2)));
            case DOWN ->
                    possibleFields.addAll(Arrays.asList(cell(x + 2, y), cell(x + 1, y + 1), cell(x, y + 2), cell(x - 1, y + 1), cell(x - 2, y)));
            case LEFT ->
                    possibleFields.addAll(Arrays.asList(cell(x, y + 2), cell(x - 1, y + 1), cell(x - 2, y), cell(x - 1, y - 1), cell(x, y - 2)));
        }
        possibleFields.removeIf(Objects::isNull);
        return possibleFields;
    }

    /**
     * Determines the next tail position of an ant based on its current head and the next head position.
     *
     * @param ant      must not be null.
     * @param nextHead must not be null.
     */
    public Cell nextTail(Ant ant, Cell nextHead) {
        Cell nextTail;
        Cell head = ant.head();
        Cell tail = ant.tail();
        if (nextHead.y() == head.y()) {
            if (nextHead.x() < head.x())
                nextTail = cell(nextHead.x() + 1, nextHead.y());
            else
                nextTail = cell(nextHead.x() - 1, nextHead.y());
        } else if (nextHead.x() == head.x()) {
            if (nextHead.y() < head.y())
                nextTail = cell(nextHead.x(), nextHead.y() + 1);
            else
                nextTail = cell(nextHead.x(), nextHead.y() - 1);
        } else if (tail.y() == head.y()) {
            if (head.y() < nextHead.y())
                nextTail = cell(nextHead.x(), nextHead.y() - 1);
            else
                nextTail = cell(nextHead.x(), nextHead.y() + 1);
        } else {
            if (nextHead.x() < head.x())
                nextTail = cell(nextHead.x() + 1, nextHead.y());
            else
                nextTail = cell(nextHead.x() - 1, nextHead.y());
        }

        return nextTail;
    }

    /**
     * Finds the nearest cell to the nest from a list of possible fields for the ant's next head position.
     *
     * @param ant must not be null
     * @return Cell closest to the nest from the possible fields for the next head position
     */
    public Cell nearestCellToNestFromPossibleFields(Ant ant) {
        List<Cell> possibleFields = possibleFieldsForNextHead(ant);
        Cell best = possibleFields.get(ThreadLocalRandom.current().nextInt(possibleFields.size()));
        double minDistance = distanceToNest(best);
        for (Cell cell : possibleFields) {
            if (cell.hasNest()) return cell;

            double newDistance = distanceToNest(cell);
            if (newDistance < minDistance) {
                minDistance = newDistance;
                best = cell;
            }
        }
        return best;
    }

    /**
     * Calculates the distance from the current cell to the nest.
     *
     * @param cell must not be null.
     */
    public double distanceToNest(Cell cell) {
        // The nest center lies in the middle of the 4 cells
        return Math.abs(cell.x() - (nestLowerCorner.x() + 0.5)) + Math.abs(cell.y() - (nestLowerCorner.y() + 0.5));
    }


    /**
     * Writes a collected leaf to the object OutputStream.
     *
     * @param leaf must not be null.
     */
    public void sendLeafToNest(Leaf leaf) {
        synchronized (objectOutputStream) {
            try {
                objectOutputStream.writeObject(leaf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns a string with the current state of the ant simulation.
     */
    public synchronized String statistic(boolean pretty) throws InterruptedException {
        freezeAll();
        StringBuilder result = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = cell(x, y);
                if (!cell.isEmpty()) {
                    Ant ant = cell.ant();
                    if (ant.head().equals(cell)) {
                        Direction direction = direction(ant);
                        result.append(direction.symbol());
                    } else {
                        if (pretty) {
                            if (ant.isLead()) {
                                if (ant.hasLeaf()) {
                                    result.append("⊛");
                                } else {
                                    result.append("*");
                                }
                            } else {
                                if (ant.hasLeaf()) {
                                    result.append("⊕");
                                } else {
                                    result.append("+");
                                }
                            }
                        } else {
                            result.append("+");
                        }
                    }
                } else if (cell.leaf() != null) {
                    result.append("X");
                } else if (cell.hasNest())
                    result.append("O");
                else {
                    int pheromone = Math.round(cell.pheromone());
                    if (pretty) {
                        result.append("_₁₂₃₄₅₆₇₈₉".charAt(pheromone));
                    } else {
                        if (pheromone == 0) {
                            result.append(" ");
                        } else {
                            result.append(pheromone);
                        }
                    }
                }
            }
            result.append("\n");
        }
        result.append("\n");
        unfreezeAll();
        return result.toString();
    }

    /**
     * Returns the direction the Ant is currently facing.
     *
     * @param ant must not be null
     */
    private Direction direction(Ant ant) {
        Cell head = ant.head();
        Cell tail = ant.tail();
        if (head.x() < tail.x())
            return Direction.LEFT;
        else if (head.x() > tail.x())
            return Direction.RIGHT;
        else if (head.y() > tail.y())
            return Direction.DOWN;
        else
            return Direction.UP;
    }


    /**
     * Tries to lock the given cells.
     * If a cell cannot be locked (because it is already locked by a different thread)
     * the locking procedure ends and any cells that were successfully are unlocked again.
     * Deadlocks are not possible because a lock is never waited for.
     *
     * @return true if all cells were successfully locked
     */
    public boolean tryLock(Cell... cells) throws InterruptedException {
        synchronized (freezeAll) {
            while (freezeAll.get()) {
                freezeAll.wait();
            }
        }
        int locked = 0;
        for (Cell cell : cells) {
            if (cell.tryLock()) {
                locked++;
                incrementLockCount();
            } else {
                break;
            }
        }

        // all cells are locked
        if (locked == cells.length) return true;

        // could not lock all, unlock those which are locked now
        for (int i = locked - 1; i >= 0; i--) {
            if (cells[i].unlock()) {
                decrementLockCount();
            }
        }
        return false;
    }

    /**
     * Freezes the world.
     * Threads trying to acquire new locks will be suspended.
     * The method returns once no locks are held.
     */
    public void freezeAll() throws InterruptedException {
        if (freezeAll.get()) {
            throw new IllegalStateException("freeze already active");
        }
        freezeAll.set(true);
        synchronized (lockCount) {
            while (lockCount.get() != 0) {
                lockCount.wait();
            }
        }
    }

    /**
     * Unfreezes the world and wakes up any suspended threads.
     */
    public void unfreezeAll() {
        synchronized (freezeAll) {
            freezeAll.set(false);
            freezeAll.notifyAll();
        }
    }

    /**
     * Locks the specific cells if possible.
     * Throws RuntimeException or IllegalStateException if the cells cannot be locked.
     * Useful when you can be sure that no other threads are holding locks to the given cells.
     */
    public void lockOrThrow(Cell... cells) {
        if (freezeAll.get()) {
            throw new IllegalStateException("Could not lock: freeze active");
        }
        for (Cell cell : cells) {
            if (!cell.tryLock()) {
                throw new RuntimeException("Could not lock");
            }
            incrementLockCount();
        }
    }

    /**
     * Releases the locks for the given cells
     */
    public void unlock(Cell... cells) {
        for (Cell cell : cells) {
            if (cell.unlock()) {
                decrementLockCount();
            }
        }
    }

    /**
     * Increments the lock count.
     */
    private void incrementLockCount() {
        lockCount.incrementAndGet();
    }

    /**
     * Decrements the lock count.
     * Throws IllegalStateException if the lock count is < 0.
     */
    private void decrementLockCount() {
        synchronized (lockCount) {
            int value = lockCount.decrementAndGet();
            if (value < 0) {
                throw new IllegalStateException("Negative lock count");
            } else if (value == 0) {
                lockCount.notifyAll();
            }
        }
    }

    /**
     * @return all four nest cells
     */
    public Cell[] nestCells() {
        return new Cell[]{
                nestLowerCorner,
                cell(nestLowerCorner.x() + 1, nestLowerCorner.y()),
                cell(nestLowerCorner.x(), nestLowerCorner.y() + 1),
                cell(nestLowerCorner.x() + 1, nestLowerCorner.y() + 1)
        };
    }
}
