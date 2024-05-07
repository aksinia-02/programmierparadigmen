package aufgabe1.world;

import aufgabe1.Randoms;
import aufgabe1.Vector;
import aufgabe1.world.entity.AntParameters;
import aufgabe1.world.entity.Colony;
import aufgabe1.world.entity.FoodSource;
import org.jetbrains.annotations.NotNull;

/**
 * This class, SimpleFoodWorldGenerator, extends WorldGenerator and is responsible for generating
 * a simple food-based world. It generates height maps and food sources in the world.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class SimpleFoodWorldGenerator extends WorldGenerator {

    public SimpleFoodWorldGenerator(int seed, @NotNull Randoms randoms, @NotNull WorldParameters parameters) {
        super(seed, randoms, parameters);
    }

    /**
     * Generates the contents of a cell in the world.
     */
    @Override
    public void generate(@NotNull Cell cell) {
        if (hashToFloat(cell.position().x(), cell.position().y()) < 0.0001) {
            generateFood(cell, 25);
        }
        cell.setHeight(generateHeight(cell.position().x(), cell.position().y()));
    }

    /**
     * Generates food sources around a given cell.
     */
    @Override
    public void generateFood(@NotNull Cell center, float amount) {
        World world = center.world();
        Randoms random = world.simulation().orElseThrow().randoms();
        int samples = (int) Math.floor(amount);
        float sigma = (float) Math.max(1, Math.sqrt(amount / 25));
        for (int i = 0; i < samples; i++) {
            int dx = (int) Math.round(random.places().nextGaussian(0, sigma));
            int dy = (int) Math.round(random.places().nextGaussian(0, sigma));

            Vector pos = new Vector(center.position()).add(dx, dy);
            Cell cell = world.get(pos);
            if (cell.foodSource() == null) {
                FoodSource source = new FoodSource(cell, amount / samples, nextFoodExpireTime());
                cell.setFoodSource(source);
                world.track(source);
            } else {
                cell.foodSource().increaseAmount(amount / samples);
            }
        }
    }

    /**
     * Generates the height for a given position in the world.
     */
    private float generateHeight(int x, int y) {
        float noise = 0;
        float persistence = 0.5f;
        float amplitude = 1;
        float lacunarity = 2;
        float frequency = 1 / 64f;
        float maxValue = 0;
        for (int octave = 0; octave < 6; octave++) {
            noise += PerlinNoise.noise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        noise = (noise / maxValue) * 0.5f + 0.5f;
        return Math.max(0.49f, noise);
    }

    /**
     * Generate the expiration time for food sources.
     */
    private int nextFoodExpireTime() {
        int time = (int) random.nextGaussian(parameters.foodExpireTimeMean, parameters.foodExpireTimeVariance);
        time = Math.max(parameters.foodExpireTimeMin, time);
        return time;
    }

    /**
     * Creates a colony in a cell.
     */
    @NotNull
    public Colony createColony(@NotNull Cell center, @NotNull Colony.Parameters colonyParameters, @NotNull AntParameters antSpawnParameters) {
        if (center.colony() != null) throw new IllegalArgumentException("Cell already has a colony");
        Colony colony = new Colony(center, colonyParameters, antSpawnParameters);
        center.setColony(colony);
        colony.spread(20 * 20);
        center.world().track(colony);
        return colony;
    }

    /**
     * Creates a colony in a cell.
     */
    @Override
    public void update(@NotNull ChunkView chunk, int time) {
        // FIXME: The generated amount depends on the chunk size
        float foodSpawnChance = 1.0f / parameters.foodExpireTimeMean;
        if (hashToFloat(chunk.hashCode(), time) < foodSpawnChance) {
            Vector pos = randomPosition(chunk);
            generateFood(chunk.get(pos.x, pos.y), 25);
        }
    }
}
