package aufgabe8;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public record World(
        @UnmodifiableView
        List<Edge> edges,
        @UnmodifiableView
        List<Node> nodes
) {
    @Contract(pure = true)
    public static int indexOf(World world, Node a, Node b) {
        if (a.index() == b.index()) throw new RuntimeException();
        if (b.index() < a.index()) return indexOf(world, b, a);
        int n = world.nodes().size() - 1, k = a.index(), l = b.index();
        // derived from
        // [nr of edges]     [row                    ]   [col  ]
        // (n^2 + n) / 2 - ( ((n - k)^2 + (n - k)) / 2 - (l - k) ) - 1
        // Note that (n^2 + n) / 2 is the gauss sum
        return -(k * (k - 2 * n + 1)) / 2 + l - 1;
    }

    /**
     * Gives the pheromone of an edge.
     */
    @Contract(pure = true)
    public static double taoOf(World world, Node a, Node b) {
        return edgeOf(world, a, b).pheromone();
    }

    /**
     * Gives the distance of an edge.
     */
    @Contract(pure = true)
    public static double distanceOf(World world, Node a, Node b) {
        return edgeOf(world, a, b).distance();
    }

    /**
     * Gives the edge of specified nodes in a world.
     */
    @Contract(pure = true)
    static Edge edgeOf(World world, Node a, Node b) {
        return world.edges().get(indexOf(world, a, b));
    }
}
