package aufgabe8;

import java.util.Random;

public record City(double x, double y) {

    /**
     * Generates a City in the given radius.
     */
    public static City random(double radius, Random random) {
        double x = random.nextDouble() * 2 * radius - radius;
        double y = random.nextDouble() * 2 * radius - radius;

        return new City(x, y);
    }
}