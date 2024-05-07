package aufgabe8;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

// simulation just acts as namespace
public record Simulation() {

    /**
     * Composes and initializes the world, tours, and parameters for the simulation.
     */
    public static Iteration setup(@UnmodifiableView List<City> cities, int m, double q0, double alpha, double beta, double rho, long seed) {
        World uninitializedWorld = createWorld(cities);
        Tour simpleTour = simpleTour(uninitializedWorld);

        // using sqrt(n) works better
        double tao0 = 1 / (Math.sqrt(cities.size()) * simpleTour.distance());
        Parameters parameters = new Parameters(q0, alpha, beta, rho, tao0, new Random(seed));

        // not sure why but giving all edges the same starting pheromone level gives better results
        World world = new World(
                uninitializedWorld.edges().stream().map(e -> new Edge(e, tao0)).toList(),
                uninitializedWorld.nodes()
        );

        List<Tour> tours = createTours(m, world.nodes(), parameters.random());

        return new Iteration(
                world, 0, simpleTour, simpleTour, tours, parameters
        );
    }


    /**
     * Generates a random city within a specified scale using the provided random number generator.
     */
    @Contract(pure = true)
    public static City randomCity(Random random, double scale) {
        double x = random.nextDouble() * 2 * scale - scale;
        double y = random.nextDouble() * 2 * scale - scale;

        return new City(x, y);
    }

    /**
     * Creates a list of tours based on the number of ants (m) and nodes in the world.
     */
    @Contract(pure = true)
    private static List<Tour> createTours(int m, List<Node> nodes, Random random) {
        return IntStream.range(0, m).mapToObj(idx -> {
            Node start = nodes.get(random.nextInt(nodes.size()));
            return new Tour(idx, start, nodes);
        }).toList();
    }

    /**
     * Creates a world with nodes and edges based on the provided list of cities.
     */
    @Contract(pure = true)
    private static World createWorld(@UnmodifiableView List<City> cities) {
        List<Node> nodes = IntStream.range(0, cities.size())
                .mapToObj(idx -> new Node(idx, cities.get(idx)))
                .toList();
        List<Edge> edges = nodes.stream()
                .flatMap(from ->
                        nodes.stream()
                                .filter(n -> n.index() > from.index())
                                .map(to -> new Edge(from, to))
                ).toList();
        return new World(edges, nodes);
    }

    /**
     * Runs the simulation for a specified number of steps, invoking the provided callback after each step.
     */
    public static Iteration run(Iteration iter, int steps, Consumer<Iteration> cb) {
        cb.accept(iter);
        if (steps == 0) return iter;
        Iteration next = Iteration.step(iter);
        return run(next, steps - 1, cb);
    }

    /**
     * Constructs a tour by visiting nodes in the world based on proximity.
     */
    private static Tour simpleTour(World world) {
        return simpleTour(new Tour(0, world.nodes().get(0), world.nodes()), world);
    }

    /**
     * Recursively builds a tour by selecting the next closest node until all nodes are visited.
     */
    private static Tour simpleTour(Tour tour, World world) {
        if (tour.openNodes().isEmpty()) return new Tour(tour, world, tour.start());
        Node next = nextClosest(tour, world);
        return simpleTour(new Tour(tour, world, next), world);
    }

    /**
     * Finds the next closest node for a given tour in the provided world.
     */
    @Contract(pure = true)
    static Node nextClosest(Tour t, World world) {
        return t.openNodes().stream()
                .map(n -> new Option(n, World.distanceOf(world, t.current(), n)))
                .min(Comparator.comparingDouble(Option::score))
                .orElseThrow().next();
    }
}
