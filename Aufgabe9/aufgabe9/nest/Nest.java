package aufgabe9.nest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Nest {
    private final List<Leaf> leaves = new ArrayList<>();
    private final BufferedWriter writer;
    private final ObjectInputStream objectInputStream;
    public Nest() {
        try {
            writer = new BufferedWriter(new FileWriter("test.out", true));
            objectInputStream = new ObjectInputStream(System.in);
            Runtime.getRuntime().addShutdownHook(new Thread(this::closeInputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Nest nest = new Nest();
        nest.startListening();
    }

    /**
     * Continuously listens to incoming objects from the input stream as Leaf instances and adds them to the leaves list.
     * Stops listening when EOF is reached and writes leaves to a file.
     */
    private void startListening() {
        try {
            // The EOFException signals the end
            //noinspection InfiniteLoopStatement
            while (true) {
                Leaf leaf = (Leaf) objectInputStream.readObject();
                leaves.add(leaf);
            }
        } catch (EOFException e) {
            writeToFile();
            System.err.println("[nest] Input stream closed, exiting.");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.err.flush();
    }

    /**
     * Writes information about the number of leaves (leaves.size()) and the content of the leaves list to the file "test.out".
     * Closes the file writer.
     */
    private void writeToFile() {
        try {
            double areaSum = leaves.stream().map(Leaf::area).reduce(0.0, Double::sum);
            System.err.printf("[nest] Collected %d leaves with a total area of %.3f.\n", leaves.size(), areaSum);
            writer.write("Leaves: size=%d, total=%.3f\n".formatted(leaves.size(), areaSum));
            writer.write("Areas: " + leaves.stream().map(Leaf::area).map(Objects::toString).collect(Collectors.joining(", ")));
            writer.write("\n\n");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the objectInputStream
     */
    private void closeInputStream() {
        try {
            objectInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
