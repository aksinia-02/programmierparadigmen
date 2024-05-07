import aufgabe8.*;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Aufgabenaufteilung:
 * Sebastian Privas: Fehlerbehebung und umschreiben zu einem funktionalen Stil, mit benützung von Streams und eigener rekursiver Funktionen, Kommentare
 * Wendelin Muth: Entwicklung der Erstversion des Algorithmus und Überarbeitung der Endversion. Überprüfung der Korrektheit.
 * Aksinia Vorobeva: Überarbeitung der Erstversion und Auslagern in verschiedene Record Files.
 *
 */
public class Test {
    public static void main(String[] args) {
        double scale = 70;
        long seed = 1337+420+69;
        int cities = 175;
        int iterations = 400;
        int m = 25;
        double q0 = 0.9;
        double alpha = 1;
        double beta = 2.1;
        double rho = 0.175;

        Random random = new Random(seed);
        Iteration start = Simulation.setup(
            Stream.generate(() -> Simulation.randomCity(random, scale)).limit(cities).toList(),
            m,
            q0,
            alpha,
            beta,
            rho,
            seed
        );

        System.out.println("Simulation parameters:");
        System.out.printf("  q0: %f\n", q0);
        System.out.printf("  alpha: %f\n", alpha);
        System.out.printf("  beta: %f\n", beta);
        System.out.printf("  rho: %f\n", rho);
        System.out.printf("  tao0: %f\n", start.parameters().tao0());
        System.out.printf("  m: %d\n", m);
        System.out.printf("  iterations: %d\n", iterations);
        System.out.printf("  cities: %d\n", cities);
        System.out.printf("  seed: %d\n", seed);
        System.out.println();

        Recording rec = new Recording();
        rec.add(start.parameters());
        rec.add(start.world().nodes());

        long startTime = System.currentTimeMillis();
        Iteration result = Simulation.run(start, iterations, (iter) -> {
            rec.add(iter);
            printIteration(scale).accept(iter);
        });
        long deltaTime = System.currentTimeMillis() - startTime;
        System.out.printf("Took %.0f seconds\n", deltaTime / 1000f);
        rec.add(deltaTime);
        rec.save();

        System.out.printf("Best tour has length: %.3f (%.3f normalized)\n", result.globalBest().distance(), result.globalBest().distance() / scale);
        printTour(result.world(), result.globalBest());
    }

    private static void printTour(World world, Tour tour) {
        System.out.printf("% 4d", tour.start().index());
        printTour(tour.visitedEdges().stream().map(idx -> world.edges().get(idx)).toList(), 1, tour.start());
        System.out.println();
    }

    private static void printTour(List<Edge> edges, int index, Node prev) {
        if(index == edges.size()) return;
        Edge edge = edges.get(index);
        Node end = edge.from() == prev ? edge.to() : edge.from();
        if(index % 5 == 0) {
            System.out.printf("\n% 4d", prev.index());
        }
        System.out.printf(" -%s-> %s",
            String.format("% 5.1f", edge.distance()).replace(" ", "-"),
            String.format("%4d", end.index()));
        printTour(edges, index+1, end);
    }

    private static Consumer<Iteration> printIteration(double scale) {
        return iter -> System.out.printf("Iteration #%d: Best (local) Length=%.3f (%.3f normalized)\n",
            iter.t(),
            iter.localBest().distance(),
            iter.localBest().distance() / scale);
    }
}
