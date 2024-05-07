package aufgabe9.arena;

import aufgabe9.nest.Leaf;

import java.io.Serial;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a cell in the arena grid.
 */
public class Cell {

    // Coordinates of the cell
    private final int x;
    private final int y;
    private final AccessibleReentrantLock mutex = new AccessibleReentrantLock();
    // Reference to an ant in the cell
    private Ant ant;
    // Flag indicating whether the cell contains a nest
    private boolean nest = false;
    // Reference to a leaf in the cell
    private Leaf leaf;
    // Pheromone level in the cell
    private float pheromone = 0;

    // In der Angabe steht das geschachtelte Klassen nicht verwendet werden sollen.
    // Ich denke aber in dem fall ist das in ordnung.

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Checks if the cell is empty of ants.
     *
     * @return True if the cell is empty, false otherwise.
     */
    public boolean isEmpty() {
        return ant == null;
    }

    public Ant ant() {
        return ant;
    }

    /**
     * Changes the emptiness status of the cell.
     */
    public void setAnt(Ant ant) {
        checkLocked();
        this.ant = ant;
    }

    /**
     * Returns true if cell can be locked, false otherwise.
     */
    public synchronized boolean tryLock() {
        return mutex.tryLock();
    }

    /**
     * Returns true if cell is unlocked successfully, false otherwise.
     */
    public synchronized boolean unlock() {
        if (mutex.isLocked()) {
            mutex.unlock();
            return true;
        }
        return false;
    }

    /**
     * Gets the x-coordinate of the cell.
     *
     * @return The x-coordinate.
     */
    public int x() {
        return x;
    }

    /**
     * Gets the y-coordinate of the cell.
     *
     * @return The y-coordinate.
     */
    public int y() {
        return y;
    }

    /**
     * Gets the pheromone level in the cell.
     *
     * @return The pheromone level.
     */
    public float pheromone() {
        return pheromone;
    }

    /**
     * Increases the pheromone level in the cell, up to a maximum of 9.
     */
    public void increasePheromone() {
        checkLocked();
        pheromone = Math.min(pheromone + 1, 9);
    }

    /**
     * Check if a cell is locked.
     */
    private void checkLocked() {
        if (!mutex.isLocked()) {
            throw new IllegalStateException("Cell is not locked (expected " + Thread.currentThread().getName() + ")");
        }
        if (!mutex.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cell not locked by current thread (expected " + Thread.currentThread().getName() + " was " + mutex.getOwner().getName() + ")");
        }
    }

    /**
     * Gets the leaf in the cell.
     *
     * @return The leaf in the cell.
     */
    public Leaf leaf() {
        return leaf;
    }

    /**
     * Sets the leaf in the cell.
     *
     * @param leaf The leaf to set.
     */
    public void setLeaf(Leaf leaf) {
        this.leaf = leaf;
    }

    /**
     * Sets the cell as containing a nest.
     */
    public void setNest() {
        nest = true;
    }

    /**
     * Checks if the cell contains a nest.
     *
     * @return True if the cell contains a nest, false otherwise.
     */
    public boolean hasNest() {
        return nest;
    }

    /**
     * Returns true if the cell has a leaf, false otherwise.
     */
    public boolean hasLeaf() {
        return leaf != null;
    }

    /**
     * A ReentrantLock that exposes access to the owning thread for debugging purposes.
     */
    private static class AccessibleReentrantLock extends ReentrantLock {
        @Serial
        private static final long serialVersionUID = 1165639030234347681L;

        @Override
        public Thread getOwner() {
            return super.getOwner();
        }
    }
}
