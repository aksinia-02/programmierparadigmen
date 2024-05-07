package aufgabe8;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;

public record Tour(int index, Node current, Node start, @UnmodifiableView List<Node> openNodes,
                   @UnmodifiableView List<Integer> visitedEdges, double distance) {
    // Create a new tour at the starting node
    public Tour(int index, Node start, List<Node> openNodes) {
        this(index, start, start, openNodes.stream().filter(n -> n != start).toList(), List.of(), 0.0);
    }

    // Create the successor of the given tour with the next node
    public Tour(Tour tour, World world, Node next) {
        this(tour.index, next, tour.start,
                tour.openNodes.stream().filter(n -> n != next).toList(),
                fastConcat(tour.visitedEdges, World.indexOf(world, tour.current, next)),
                tour.distance + World.distanceOf(world, tour.current, next));
    }

    /**
     * This function temporarily breaks immutability for a minor performance gain
     */
    @UnmodifiableView
    @Contract(pure = true)
    private static <T> List<T> fastConcat(List<T> list, T value) {
        List<T> result = new ArrayList<>(List.copyOf(list));
        result.add(value);
        return result;
    }
}
