package aufgabe8;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * This class is only used for debugging and is not part of the solution.
 * For simplicity, it is written in an OO-Style
 */
public class Recording {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###E0", new DecimalFormatSymbols(Locale.ENGLISH));

    static {
        DECIMAL_FORMAT.setGroupingUsed(false);
    }

    private final StringBuilder sb = new StringBuilder();

    private static String format(double d) {
        return DECIMAL_FORMAT.format(d);
    }

    public void add(Parameters parameters) {
        sb.append("m ").append(parameters.toString().replace("\n", "\\n")).append("\n");
    }

    public void add(List<Node> nodes) {
        nodes.forEach(node -> {
            sb.append("n ")
                    .append(format(node.city().x())).append(" ")
                    .append(format(node.city().y())).append("\n");
        });
    }

    public void add(Iteration iter) {
        sb.append("i ").append(iter.globalBest().distance()).append("\n");
        iter.globalBest().visitedEdges().forEach(idx -> {
            Edge edge = iter.world().edges().get(idx);
            sb.append("e ")
                    .append(edge.from().index()).append(" ")
                    .append(edge.to().index()).append(" ")
                    .append(format(edge.pheromone())).append("\n");
        });
    }

    public void add(long deltaTime) {
        sb.append("f ").append(deltaTime).append("\n");
    }

    public void save() {
        try {
            Files.writeString(Path.of("./Aufgabe8/simulation-" + System.currentTimeMillis() + ".recording"),
                    sb.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            // ignored
            System.out.println("Debug Recording could not be saved");
        }

    }
}
