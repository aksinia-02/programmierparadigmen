package aufgabe1;

import org.jetbrains.annotations.NotNull;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Provides the simulation with random number generators.
 * Modularisierungseinheit: Klasse
 */
public final class Randoms {
    @NotNull
    private final RandomGeneratorFactory<?> factory;
    private final long seed;
    @NotNull
    private RandomGenerator ants;
    @NotNull
    private RandomGenerator places;

    public Randoms(@NotNull RandomGeneratorFactory<?> factory, long seed) {
        this.factory = factory;
        this.seed = seed;
        ants = factory.create(seed);
        places = factory.create(seed);
    }

    /**
     * Resets the random generators to their initial state
     */
    @SuppressWarnings("unused")
    public void reset() {
        ants = factory.create(seed);
        places = factory.create(seed);
    }

    /**
     * Creates a new random number generator with a given seed.
     */
    public RandomGenerator newRandom(long seed) {
        return factory.create(seed);
    }

    @NotNull
    public RandomGenerator places() {
        return places;
    }

    @NotNull
    public RandomGenerator ants() {
        return ants;
    }
}
