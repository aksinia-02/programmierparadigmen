package aufgabe9.arena;

import aufgabe9.World;
import aufgabe9.nest.Leaf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * The Arena class represents the environment where ants and other elements interact.
 */
public class Arena {

    // List to store Ant objects
    private final List<Ant> ants = new ArrayList<>();
    // List to store Thread objects associated with Ants
    private final List<Thread> antThreads = new ArrayList<>();
    // The world in which the ants and other elements exist
    private final World world;
    // Number of ants in the arena
    private final int numberOfAnts;
    // Output stream for object serialization
    private final ObjectOutputStream objectOutputStream;
    // Atomic boolean to signal stopping the arena
    private final AtomicBoolean stopSignal = new AtomicBoolean(false);
    // BufferedWriter for writing to a file
    private BufferedWriter writer;

    /**
     * Constructor to initialize the arena with the number of ants and world dimensions.
     *
     * @param numberOfAnts Number of ants in the arena.
     * @param width        Width of the world.
     * @param height       Height of the world.
     */
    public Arena(int numberOfAnts, int width, int height) {
        this.numberOfAnts = numberOfAnts;
        try {
            objectOutputStream = new ObjectOutputStream(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        world = new World(width, height, objectOutputStream);
        openFile();
    }

    /**
     * Main method to start the arena with command line arguments.
     *
     * @param args Command line arguments specifying the number of ants and world dimensions.
     */
    public static void main(String[] args) {
        Arena arena = new Arena(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        arena.start();
    }

    /**
     * Method to interrupt the arena and stop its execution.
     */
    public void interruptArena() {
        synchronized (stopSignal) {
            stopSignal.set(true);
            stopSignal.notify();
        }
    }

    /**
     * Writes the world statistics to the opened file.
     * The statistics are obtained from the World's statistic() method.
     * The result is written to the file followed by a separator line.
     * If an IOException occurs during writing, it is wrapped in a RuntimeException.
     */
    public synchronized void writeStateToFile(int waitCount) throws InterruptedException {
        String resultString = world.statistic(false);
        try {
            writer.write("State of world after %d waits:\n".formatted(waitCount));
            writer.write(resultString);
            writer.write("--------\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to start the arena, spawning ants, leaves, and nest, and controlling the main loop.
     */
    private void start() {
        // Place ants but start them later
        List<Thread> antThreads = IntStream.range(0, numberOfAnts)
                .mapToObj(this::spawnAnt).toList();

        int area = world.height() * world.width();
        int leaves = (area * 5) / 100;
        for (int i = 0; i < leaves; i++) {
            spawnLeaf();
        }
        spawnNest();

        antThreads.forEach(Thread::start);

        try {
            while (!stopSignal.get()) {
                // on some systems wait can return without
                // notify being called.
                // The loop ensures that this won't be an issue.
                synchronized (stopSignal) {
                    stopSignal.wait();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.err.println("[arena] Stop signal received.");
        System.err.println("[arena] Stopping ant threads.");
        interruptAndJoinAntThreads();
        printAnts();
        System.err.println("[arena] Closing resources.");
        closeResources();
        System.err.println("[arena] Exiting.");
        System.err.flush();
    }

    private void printAnts() {
        try {
            for (Ant ant : ants) {
                writer.write("Ant@%s: waited=%d, steps=%d, head=%d;%d, tail=%d;%d\n".formatted(
                        Integer.toHexString(ant.hashCode()),
                        ant.waitCount(),
                        ant.stepCount(),
                        ant.head().x(),
                        ant.head().y(),
                        ant.tail().x(),
                        ant.tail().x()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to spawn an ant and start its associated thread.
     *
     * @param i Index of the ant.
     */
    private Thread spawnAnt(int i) {
        Ant ant;
        if (i == 0)
            ant = new Ant(this, world, true);
        else
            ant = new Ant(this, world, false);
        Thread thread = new Thread(null, ant, "ant-%02d".formatted(i), 128000);
        antThreads.add(thread);
        ants.add(ant);
        placeAntInWorld(ant);

        return thread;
    }

    /**
     * Method to interrupt all ant threads.
     */
    private void interruptAndJoinAntThreads() {
        for (Thread antThread : antThreads) {
            antThread.interrupt();
        }
        for (Thread antThread : antThreads) {
            try {
                antThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Method to place an ant in the world by choosing a random cell and neighboring cell.
     *
     * @param ant The ant to be placed in the world.
     */
    private void placeAntInWorld(Ant ant) {
        int areaOccupiedByAnts = ants.size() * 2 + 2;
        int areaAvailable = world.width() * world.height();
        if ((float) areaOccupiedByAnts > (2f / 3f) * areaAvailable) {
            throw new IllegalStateException("Too many ants! At least 1/3 of all spaces must remain unoccupied. You must choose less ants!");
        }

        Cell head = null;
        Cell tail = null;
        while (tail == null) {
            head = chooseRandomCell();
            tail = chooseNeighboringCell(head);
        }
        world.lockOrThrow(head, tail);
        ant.setHead(head);
        ant.setTail(tail);
        world.unlock(head, tail);
    }


    /**
     * Method to choose a random empty cell in the world.
     *
     * @return A randomly chosen empty cell.
     */
    private Cell chooseRandomCell() {
        Cell cell;
        do {
            int x = ThreadLocalRandom.current().nextInt(world.width());
            int y = ThreadLocalRandom.current().nextInt(world.height());
            cell = world.cell(x, y);
        } while (!cell.isEmpty());
        return cell;
    }

    /**
     * Method to choose a neighboring empty cell for a given cell.
     *
     * @param cell The reference cell.
     * @return A neighboring empty cell.
     */
    private Cell chooseNeighboringCell(Cell cell) {
        int count = 0;
        Cell result = null;
        List<Cell> cells = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Cell newCell = world.cell(cell.x() + dx, cell.y() + dy);
                if (Math.abs(dx) != Math.abs(dy) && newCell != null)
                    cells.add(newCell);
            }
        }
        while (count != cells.size() && result == null) {
            int random = ThreadLocalRandom.current().nextInt(cells.size());
            if (cells.get(random).isEmpty())
                result = cells.get(random);
            count++;
        }
        return result;
    }

    /**
     * Method to spawn a leaf in a random empty cell in the world.
     */
    private void spawnLeaf() {
        Cell leafCell = null;
        while (leafCell == null || leafCell.leaf() != null) {
            int x = ThreadLocalRandom.current().nextInt(world.width());
            int y = ThreadLocalRandom.current().nextInt(world.height());
            leafCell = world.cell(x, y);
            // Don't spawn leafs next to nest
            if (world.distanceToNest(leafCell) <= 2.01) leafCell = null;
        }
        leafCell.setLeaf(new Leaf());
    }

    /**
     * Method to spawn a nest in the world.
     */
    private void spawnNest() {
        for (Cell cell : world.nestCells()) {
            cell.setNest();
        }
    }

    /**
     * Method to close resources, such as the BufferedWriter and ObjectOutputStream.
     */
    private void closeResources() {
        try {
            writer.close();
            synchronized (objectOutputStream) {
                objectOutputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens a file for writing and initializes the BufferedWriter.
     * The file is named "test.out" and is opened in append mode.
     * If an IOException occurs, it is wrapped in a RuntimeException.
     */
    private void openFile() {
        try {
            writer = new BufferedWriter(new FileWriter("test.out", true));
            writer.write("===== Start =====\nParameters: width=%d, height=%d, ants=%d\n\n".formatted(world.width(), world.height(), numberOfAnts));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
