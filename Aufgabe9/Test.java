import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Aufgabenaufteilung:
 * Sebastian Privas: IPC, test.out, Test Ausführung und test timeout
 * Wendelin Muth: Synchronisation, Bewegung der Ameisen, Testen, Verschiedenes, IPC
 * Aksinia Vorobeva: Initiale Lösung, Bewegung der Ameisen, Erstellung der Ameisenwelt
 */
public class Test {
    public static void main(String[] args) {
        if (args.length != 3) {
            // Laut angabe: "Die Anzahl der Ameisen in der Arena und die Länge und Breite der Arena"
            System.err.println("[main] Expected arguments: <ants> <height> <width>");
            System.exit(-1);
        }
        long startTime = System.currentTimeMillis();
        int numberOfAnts = parseIntArg("ants", args[0]);
        int height = parseIntArg("height", args[1]);
        int width = parseIntArg("width", args[2]);

        if (width <= 4 || width > 80) throw new IllegalArgumentException("width must be > 4 and <= 80");
        if (height <= 4 || height > 80) throw new IllegalArgumentException("height must be > 4 and <= 80");

        File file = new File("test.out");
        file.delete();

        int upperBound = 80;
        int lowerBound = 5;
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean timoutOcurred = new AtomicBoolean(false);
        for (int i = 0; i < 3 && !timoutOcurred.get(); i++) {

            ProcessBuilder arenaProcessBuilder = new ProcessBuilder(makeCommand("aufgabe9.arena.Arena", new int[]{numberOfAnts, width, height}));
            ProcessBuilder nestProcessBuilder = new ProcessBuilder(makeCommand("aufgabe9.nest.Nest", new int[]{}));

            arenaProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            nestProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            System.err.printf("[main] Starting pipeline. Parameters: ants=%d, width=%d, height=%d\n", numberOfAnts, width, height);

            final List<Process> childProcesses;
            try {
                // Nothing but startPipeline works
                childProcesses = ProcessBuilder.startPipeline(List.of(arenaProcessBuilder, nestProcessBuilder));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> childProcesses.forEach(Process::destroy), "subprocess-killer"));

            ScheduledFuture<?> timeLimitFuture = executorService.schedule(() -> {
                System.err.println("[watchdog] Timeout! Exiting...");
                // Terminate the child processes if the time limit is reached
                childProcesses.forEach(Process::destroy);
                timoutOcurred.set(true);
            }, 3, TimeUnit.SECONDS);

            AtomicBoolean exitCodeZero = new AtomicBoolean(true);
            List<CompletableFuture<Void>> threads = List.of(
                    CompletableFuture.runAsync(() -> {
                        try {
                            int exitCode = childProcesses.get(0).waitFor();
                            exitCodeZero.compareAndSet(true, exitCode == 0);
                            System.err.printf("[main] Arena process exited with code: %d\n", exitCode);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }),
                    CompletableFuture.runAsync(() -> {
                        try {
                            int exitCode = childProcesses.get(1).waitFor();
                            exitCodeZero.compareAndSet(true, exitCode == 0);
                            System.err.printf("[main] Nest process exited with code: %d\n", exitCode);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    })
            );

            threads.forEach(CompletableFuture::join);

            if (!exitCodeZero.get()) {
                System.err.println("[main] A subprocess exited with non zero code! Stopping Test.");
                System.exit(-1);
            }

            numberOfAnts += 5;
            height = (height + 5 - lowerBound) % (upperBound - lowerBound) + lowerBound;
            width = (width + 5 - lowerBound) % (upperBound - lowerBound) + lowerBound;
            timeLimitFuture.cancel(true);
        }
        executorService.shutdown();
        System.err.printf("[main] The Test finished after %d milliseconds.\n", System.currentTimeMillis() - startTime);
    }

    public static List<String> makeCommand(String className, int[] arguments) {
        // Just copy the cp of the parent process to the child processes.
        // Make sure the parent process's cp includes all .class files.
        String classPath = System.getProperty("java.class.path");

        List<String> commandList = new ArrayList<>();
        commandList.add("java");  // Add the Java executable
        commandList.add("-cp");
        commandList.add(classPath);
        commandList.add(className);  // Add the Arena class
        for (int argument : arguments) {
            commandList.add(String.valueOf(argument));
        }
        return commandList;
    }

    private static int parseIntArg(String name, String str) {
        int inputInt = -1;
        try {
            inputInt = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Arg <%s>: Expected an integer, got: \"%s\"".formatted(name, str));
        }
        return inputInt;
    }
}
