package aufgabe8;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record Iteration(World world, int t, Tour localBest, Tour globalBest, @UnmodifiableView List<Tour> tours,
                        Parameters parameters) {

    /**
     * Updates tours based on pheromone levels and selects the best tour.
     */
    @Contract(pure = true)
    public static Iteration step(Iteration iter) {
        List<Tour> tours = iter.tours().stream().map(tour -> new Tour(tour.index(), tour.start(), iter.world().nodes())).toList();
        TourContext context = TourContext.walkEveryTour(new TourContext(tours, iter.world()), iter.world().nodes().size() - 1, iter.parameters());
        Tour localBest = context.tours().stream()
                .min(Comparator.comparingDouble(Tour::distance))
                .orElseThrow();
        Tour globalBest = Stream.of(localBest, iter.globalBest())
                .min(Comparator.comparingDouble(Tour::distance))
                .orElseThrow();
        return new Iteration(
                new World(updatePheromones(globalBest, iter.parameters(), context.world()), iter.world().nodes()),
                iter.t + 1, localBest, globalBest, tours, iter.parameters()
        );
    }

    /**
     * Updates pheromone levels on edges based on the best tour's information.
     */
    @Contract(pure = true)
    private static List<Edge> updatePheromones(Tour gb, Parameters parameters, World world) {
        double delta = 1.0 / gb.distance();

        return world.edges().stream().map(edge -> {
            if (gb.visitedEdges().contains(World.indexOf(world, edge.from(), edge.to()))) {
                return updatePheromone(edge, parameters.rho(), delta);
            } else {
                return updatePheromone(edge, parameters.rho());
            }
        }).toList();
    }

    /**
     * Updates the pheromone level on an edge considering a given formula
     */
    @Contract(pure = true)
    private static Edge updatePheromone(Edge edge, double rho, double delta) {
        return new Edge(edge, (1 - rho) * edge.pheromone() + rho * delta);
    }

    /**
     * Updates the pheromone level on an edge considering a given formula
     */
    @Contract(pure = true)
    private static Edge updatePheromone(Edge edge, double rho) {
        return new Edge(edge, (1 - rho) * edge.pheromone());
    }

}
