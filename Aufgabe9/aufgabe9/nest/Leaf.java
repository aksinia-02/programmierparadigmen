package aufgabe9.nest;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class Leaf implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final double area;

    public Leaf() {
        area = ThreadLocalRandom.current().nextDouble() * 5;
    }

    private Leaf(double area) {
        this.area = area;
    }

    /**
     * Returns a part of the leaf.
     */
    public Leaf cutPart() {
        double cutArea = ThreadLocalRandom.current().nextDouble() * area / 2;
        // Wir lassen das Blatt gleich groß wenn ein Stück abgeschnitten wird. Begründung:
        // "Die Blattschneiderameisen schneiden sich nur ein Stück ab und für die Simulation kann das beliebig oft sein,
        // z.B. weil ein Blatt immer nachwächst. Also die Blätter verschwinden nie."

        // leafArea -= cutArea;
        return new Leaf(cutArea);
    }

    @Override
    public String toString() {
        return "area: " + area;
    }

    public double area() {
        return area;
    }
}
