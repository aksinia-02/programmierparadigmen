package aufgabe9.arena;

import aufgabe9.World;
import aufgabe9.nest.Leaf;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Ant implements Runnable {

    private final Arena arena;
    private final World world;
    private final boolean isLead;
    private Cell head, tail;
    private int waitCount = 0, stepCount = 0;
    private Leaf leaf;


    public Ant(Arena arena, World world, boolean isLead) {
        this.arena = arena;
        this.world = world;
        this.isLead = isLead;
    }


    /**
     * Runs the ant simulation, where an ant alternates between moving randomly and moving towards the nest.
     * The simulation continues until the ant thread is interrupted.
     */
    @Override
    public void run() {
        try {
            // An ant runs until it is interrupted
            //noinspection InfiniteLoopStatement
            while (true) {
                if (leaf == null) {
                    tryMoveRandom();
                } else {
                    tryMoveToNest();
                }
                sleepRandom();
            }
        } catch (InterruptedException e) {
            System.err.printf("[%s] Interrupted, exiting.\n", Thread.currentThread().getName());
        }
    }

    /**
     * Causes the ant to sleep for a random duration between 5 and 50 milliseconds.
     * Increments the wait count and writes the state to a file if the ant is a lead ant.
     *
     * @throws InterruptedException If the ant thread is interrupted while sleeping.
     */
    private void sleepRandom() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextInt(46) + 5);
        increaseWaitCount();
        if (isLead)
            arena.writeStateToFile(waitCount);
    }

    private void rank(List<Cell> cells) {
        Collections.shuffle(cells);
        cells.sort((a, b) -> -Double.compare(rank(a), rank(b)));
    }

    /**
     * Ranks a cell based on various factors such as the presence of other ants, leaves, and pheromone levels.
     *
     * @param cell The cell to be ranked.
     * @return The rank score for the given cell.
     */
    private double rank(Cell cell) {
        double score = 0;
        if (cell.ant() != this && !cell.isEmpty()) {
            score -= 100;
        }
        if (cell.hasLeaf()) {
            score += 10;
            score += Math.sqrt(1 / (world.distanceToNest(cell)));
        } else {
            score += cell.pheromone();
        }
        return score;
    }

    /**
     * Attempts to move the ant's head and tail to the specified cells, possibly increasing pheromone levels.
     *
     * @param nextHead          The target cell for the ant's head.
     * @param increasePheromone Whether to increase pheromone levels during the move.
     * @return True if the move is successful, false otherwise.
     * @throws InterruptedException If the ant thread is interrupted while attempting the move.
     */
    private boolean tryMoveTo(Cell nextHead, boolean increasePheromone) throws InterruptedException {
        Cell nextTail = world.nextTail(this, nextHead);
        if (!world.tryLock(head, tail, nextHead, nextTail)) {
            return false;
        }

        if (!nextHead.isEmpty() || !nextTail.isEmpty()) {
            world.unlock(head, tail, nextHead, nextTail);
            return false;
        }

        Cell prevHead = head;
        Cell prevTail = tail;
        setHead(nextHead);
        setTail(nextTail);

        if (increasePheromone) {
            nextHead.increasePheromone();
            nextTail.increasePheromone();
        }
        increaseStepCount();

        world.unlock(prevHead, prevTail, nextHead, nextTail);
        return true;
    }

    /**
     * Tries to move the ant randomly within the world, avoiding nest cells.
     * If the move is successful, checks for a leaf on the new cell and cuts a part of it.
     *
     * @throws InterruptedException If the ant thread is interrupted while attempting the move.
     */
    private void tryMoveRandom() throws InterruptedException {
        List<Cell> possibleFields = world.possibleFieldsForNextHead(this);
        rank(possibleFields);
        // keep wandering ants off of the nest
        possibleFields.removeIf(Cell::hasNest);

        boolean didMove = false;
        for (Cell field : possibleFields) {
            if (tryMoveTo(field, false)) {
                didMove = true;
                break;
            }
        }

        if (didMove) {
            Leaf targetLeaf = head.leaf();
            if (targetLeaf != null) {
                leaf = targetLeaf.cutPart();
            }
        }
    }

    /**
     * Tries to move the ant towards the nest.
     * If the move is successful, checks for leaf delivery to the nest.
     * If the ant cannot move, it has a chance to get unstuck by trying to move randomly in a different direction.
     *
     * @throws InterruptedException If the ant thread is interrupted while attempting the move.
     */
    private void tryMoveToNest() throws InterruptedException {
        Cell nextHead = world.nearestCellToNestFromPossibleFields(this);

        boolean didMove = false;
        if (nextHead.hasNest()) {
            Cell[] nest = world.nestCells();
            if (!world.tryLock(nest)) {
                return;
            }
            boolean allEmpty = true;
            for (Cell cell : nest) {
                allEmpty = cell.ant() == this || cell.isEmpty();
                if (!allEmpty) break;
            }
            world.unlock(nest);
            // can only move to nest if all 4 entrance cells are empty
            if (allEmpty) {
                didMove = tryMoveTo(nextHead, true);
            }
        } else {
            didMove = tryMoveTo(nextHead, true);
        }

        if (head.hasNest()) {
            sendLeafToNest();
        } else if (!didMove && ThreadLocalRandom.current().nextFloat() <= 0.5) {
            // If the ant cannot move, it is probably stuck
            // This gives it a chance to get unstuck
            List<Cell> possibleFields = world.possibleFieldsForNextHead(this);
            possibleFields.removeIf(Cell::hasNest);
            Collections.shuffle(possibleFields);
            possibleFields.sort(Comparator.comparingDouble(world::distanceToNest).reversed());

            for (Cell field : possibleFields) {
                if (tryMoveTo(field, false)) {
                    break;
                }
            }
        }
    }

    /**
     * Sets the ant's head to the specified cell, updating the cell's ant reference.
     *
     * @param cell The new cell to be the ant's head.
     */
    public void setHead(Cell cell) {
        if (head != null)
            head.setAnt(null);
        if (cell != null)
            cell.setAnt(this);
        head = cell;
    }

    /**
     * Sets the ant's tail to the specified cell, updating the cell's ant reference.
     *
     * @param cell The new cell to be the ant's tail.
     */
    public void setTail(Cell cell) {
        if (tail != null)
            tail.setAnt(null);
        if (cell != null)
            cell.setAnt(this);
        tail = cell;
    }

    /**
     * Retrieves the cell that is currently the ant's head.
     *
     * @return The cell that is the ant's head.
     */
    public Cell head() {
        return head;
    }

    /**
     * Retrieves the cell that is currently the ant's tail.
     *
     * @return The cell that is the ant's tail.
     */
    public Cell tail() {
        return tail;
    }

    /**
     * Increases the ant's wait count. If the count reaches a threshold (64), interrupts the arena.
     */
    private void increaseWaitCount() {
        waitCount++;
        if (waitCount == 64) {
            arena.interruptArena();
        }
    }

    /**
     * Increases the ant's step count, representing the number of steps taken by the ant.
     */
    private void increaseStepCount() {
        stepCount++;
    }

    /**
     * Delivers the ant's leaf to the nest, resetting the leaf reference to null.
     */
    private void sendLeafToNest() {
        world.sendLeafToNest(leaf);
        leaf = null;
    }

    /**
     * Checks if the ant is a lead ant.
     *
     * @return True if the ant is a lead ant, false otherwise.
     */
    public boolean isLead() {
        return isLead;
    }

    /**
     * Checks if the ant is carrying a leaf.
     *
     * @return True if the ant has a leaf, false otherwise.
     */
    public boolean hasLeaf() {
        return leaf != null;
    }

    /**
     * Retrieves the ant's current wait count.
     *
     * @return The ant's wait count.
     */
    public int waitCount() {
        return waitCount;
    }

    /**
     * Retrieves the ant's current step count.
     *
     * @return The ant's step count.
     */
    public int stepCount() {
        return stepCount;
    }
}
