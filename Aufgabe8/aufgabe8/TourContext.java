package aufgabe8;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public record TourContext(@UnmodifiableView List<Tour> tours, World world) {

    /**
     * Steps through each tour in the context, updating the current node of each tour.
     */
    @Contract(pure = true)
    static TourContext stepEveryTour(TourContext context, int index, Parameters parameters, Function<Parameters, Function<World, Function<Tour, Node>>> nextNodeFunc) {
        if (index == context.tours().size()) return context;

        Tour tour = context.tours().get(index);
        TourContext next = goToNext(context, tour, parameters, nextNodeFunc.apply(parameters).apply(context.world()).apply(tour));
        return stepEveryTour(next, index + 1, parameters, nextNodeFunc);
    }

    @Contract(pure = true)
    private static Function<Parameters, Function<World, Function<Tour, Node>>> goToNext() {
        return parameters -> world -> tour -> nextNode(tour, parameters, world);
    }

    @Contract(pure = true)
    private static Function<Parameters, Function<World, Function<Tour, Node>>> goToStart() {
        return parameters -> world -> Tour::start;
    }

    /**
     * Updates the pheromone level from a given formula, on an edge after an ant moves to the next node.
     */
    @Contract(pure = true)
    private static Edge updatePheromone(Edge edge, double rho, double tao0) {
        return new Edge(edge, (1 - rho) * edge.pheromone() + rho * tao0);
    }

    /**
     * Moves the tour to the next node and updates pheromones accordingly.
     */
    @Contract(pure = true)
    private static TourContext goToNext(TourContext context, Tour tour, Parameters parameters, Node next) {
        Edge edge = updatePheromone(World.edgeOf(context.world(), tour.current(), next), parameters.rho(), parameters.tao0());
        World world = new World(
                fastModify(context.world().edges(), World.indexOf(context.world(), edge.from(), edge.to()), edge),
                context.world().nodes()
        );
        return new TourContext(fastModify(context.tours(), tour.index(), new Tour(tour, world, next)), world);
    }

    /**
     * Walks through every tour in the context for a specified number of steps, updating their nodes.
     */
    @Contract(pure = true)
    public static TourContext walkEveryTour(TourContext context, int steps, Parameters parameters) {
        if (steps == 0) return stepEveryTour(context, 0, parameters, goToStart());

        TourContext next = stepEveryTour(context, 0, parameters, goToNext());
        return walkEveryTour(next, steps - 1, parameters);
    }

    /**
     * Determines the next node for an ant based on q0 probability and heuristic information.
     */
    @Contract(pure = true)
    static Node nextNode(Tour t, Parameters parameters, World world) {
        if (parameters.random().nextDouble() > parameters.q0()) {
            return nextRandom(t, parameters, world);
        }
        return nextBest(t, world);
    }

    /**
     * Determines the next node for an ant by random selection using CDF.
     */
    @Contract(pure = true)
    static Node nextRandom(Tour tour, Parameters parameters, World world) {
        List<Option> options = tour.openNodes().stream()
                .map(n -> new Option(world, tour.current(), n, parameters))
                .toList();
        double total = options.stream().map(Option::score).reduce(0.0, Double::sum);
        double threshold = parameters.random().nextDouble() * total;
        // Use cumulative distribution function to pick amongst weighted options
        return options.stream()
                .reduce(
                        new Option(null, 0.0),
                        (result, opt) ->
                                // keep summing until the sum is greater than then threshold
                                result.score() > threshold ?
                                        result :
                                        new Option(opt.next(), result.score() + opt.score())
                ).next();
    }

    /**
     * Determines the next node for an ant by selecting the best option based on heuristic information.
     */
    @Contract(pure = true)
    static Node nextBest(Tour tour, World world) {
        return tour.openNodes().stream()
                .map(n -> new Option(world, tour.current(), n))
                .max(Comparator.comparingDouble(Option::score))
                .orElseThrow().next();
    }

    /**
     * This function temporarily breaks immutability for a drastic performance gain.
     */
    @Contract(pure = true)
    @UnmodifiableView
    private static <T> List<T> fastModify(@UnmodifiableView List<T> list, int index, T value) {
        List<T> result = new ArrayList<>(List.copyOf(list));
        result.set(index, value);
        return Collections.unmodifiableList(result);
    }

}