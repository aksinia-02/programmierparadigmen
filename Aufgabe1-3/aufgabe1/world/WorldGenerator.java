package aufgabe1.world;

import aufgabe1.IVector;
import aufgabe1.Randoms;
import aufgabe1.Vector;
import aufgabe1.world.entity.AntParameters;
import aufgabe1.world.entity.Colony;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Represents an abstraction of the generation of the content of the world.
 * STYLE: This class follows the object-oriented programming paradigm, because it is an abstraction of methods like
 * generateFood, update, createColony etc. It also consists of a subtyping class ChunkView.
 */
public abstract class WorldGenerator {

    @NotNull
    private final Randoms randoms;
    protected int seed;
    @NotNull
    protected RandomGenerator random;
    @NotNull
    protected WorldParameters parameters;
    private boolean locked;


    public WorldGenerator(int seed, @NotNull Randoms randoms, @NotNull WorldParameters parameters) {
        Objects.requireNonNull(randoms);
        Objects.requireNonNull(parameters);
        random = randoms.newRandom(seed);
        this.seed = seed;
        this.randoms = randoms;
        this.parameters = parameters;
    }

    public void setSeed(int seed) {
        this.seed = seed;
        random = randoms.newRandom(seed);
    }

    /**
     * While the generator is locked, no chunks will be populated
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    protected boolean locked() {
        return locked;
    }

    public abstract void generate(@NotNull Cell cell);

    public abstract void generateFood(@NotNull Cell center, float amount);

    @NotNull
    public abstract Colony createColony(@NotNull Cell center, @NotNull Colony.Parameters colonyParameters, @NotNull AntParameters antSpawnParameters);

    public abstract void update(ChunkView chunk, int time);

    @NotNull
    protected Vector randomPosition(@NotNull ChunkView chunk) {
        IVector size = chunk.size();
        int dx = random.nextInt(size.x());
        int dy = random.nextInt(size.y());

        return new Vector(chunk.origin()).add(dx, dy);
    }

    protected float hashToFloat(int... values) {
        int hash = hash(values);

        int ieeeMantissa = 0x007FFFFF;
        int ieeeOne = 0x3F800000;

        hash &= ieeeMantissa;
        hash |= ieeeOne;
        float f = Float.intBitsToFloat(hash);
        return f - 1;
    }

    // Jenkins hash function
    protected int hash(int @NotNull ... values) {
        int hash = seed;
        hash += hash << 10;
        hash ^= hash >> 6;
        for (int value : values) {
            hash += value;
            hash += hash << 10;
            hash ^= hash >> 6;
        }
        hash += hash << 3;
        hash ^= hash >> 11;
        hash += hash << 15;
        return hash;
    }

    @SuppressWarnings("unused")
    public void setParameters(@NotNull WorldParameters parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
    }

    @SuppressWarnings("unused")
    @NotNull
    public WorldParameters parameters() {
        return parameters;
    }
}
