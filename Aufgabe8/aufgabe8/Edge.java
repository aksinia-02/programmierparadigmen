package aufgabe8;

import org.jetbrains.annotations.Contract;

public record Edge(Node from, Node to, double distance, double pheromone) {
    public Edge(Edge edge, double pheromone) {
        this(edge.from, edge.to, edge.distance, pheromone);
    }

    public Edge(Node from, Node to) {
        this(from, to, eulerDistance(from.city().x() - to.city().x(), from.city().y() - to.city().y()), 0.0);
    }

    /**
     * Calculates the euler distance with the given coordinates.
     */
    @Contract(pure = true)
    private static double eulerDistance(double dx, double dy) {
        return Math.sqrt(dx * dx + dy * dy);
    }
}
