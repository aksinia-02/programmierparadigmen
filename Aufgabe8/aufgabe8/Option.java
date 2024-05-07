package aufgabe8;

import org.jetbrains.annotations.Contract;

public record Option(Node next, double score) {
    public Option(World world, Node current, Node next) {
        this(next, score(world, current, next));
    }

    /**
     * Creates an option with the calculated score.
     */
    public Option(World world, Node current, Node next, Parameters parameters) {
        this(next, score(world, current, next, parameters.alpha(), parameters.beta()));
    }

    /**
     * Calculates the score based on pheromone and distance between nodes.
     */
    @Contract(pure = true)
    private static double score(World world, Node current, Node next) {
        return World.taoOf(world, current, next) * (1.0 / World.distanceOf(world, current, next));
    }

    /**
     * Calculates the score based on pheromone, distance, and additional parameters.
     */
    @Contract(pure = true)
    private static double score(World world, Node current, Node next, double alpha, double beta) {
        return
                Math.pow(World.taoOf(world, current, next), alpha) *
                        Math.pow(1 / World.distanceOf(world, current, next), beta);
    }
}
