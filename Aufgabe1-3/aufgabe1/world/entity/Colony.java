package aufgabe1.world.entity;

import aufgabe1.Direction;
import aufgabe1.IVector;
import aufgabe1.Randoms;
import aufgabe1.Vector;
import aufgabe1.world.Cell;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * Represents a colony of ants in the simulation world. All ants belong to one colony.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class Colony extends Entity {
    @NotNull
    private final Set<Ant> ants = new HashSet<>();
    @NotNull
    private final List<Cell> cells = new ArrayList<>();
    // is always >= 0
    private float food;
    @NotNull
    private AntParameters antSpawnParameters;

    @NotNull
    private Colony.Parameters parameters;

    public Colony(@NotNull Cell cell, @NotNull Colony.Parameters parameters, @NotNull AntParameters antSpawnParameters) {
        super(cell);
        Objects.requireNonNull(antSpawnParameters);
        Objects.requireNonNull(parameters);
        this.cell = cell;
        this.antSpawnParameters = antSpawnParameters;
        this.parameters = parameters;
        this.cells.add(cell);
    }

    /**
     * Removes an ant from the colony.
     *
     * @param ant The ant to be removed.
     */
    public void removeAnt(Ant ant) {
        ants.remove(ant);
    }

    /**
     * Updates the colony, checking if conditions are met to spawn a new ant.
     */
    @Override
    public void update() {
        if (food >= parameters.antSpawnFoodThreshold + parameters.antSpawnFoodCost) {
            spawnAnt(0, 0);
            decreaseFood(parameters.antSpawnFoodCost);
        }
    }

    /**
     * Spawns an ant near the colony within the specified radius.
     *
     * @param minRadius must be >= 0 and <= the max radius
     * @param maxRadius must be >= 0 and >= the min radius
     * @return the spawned {@link Ant}
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    public Ant spawnAnt(int minRadius, int maxRadius) {
        Randoms random = cell.world().simulation().orElseThrow().randoms();
        double angle = random.ants().nextDouble(2 * Math.PI);
        double radius = minRadius;
        if (minRadius != maxRadius) {
            radius += random.ants().nextDouble(maxRadius - minRadius);
        }
        int dx = (int) Math.round(Math.cos(angle) * radius);
        int dy = (int) Math.round(Math.sin(angle) * radius);

        IVector pos = new Vector(position()).add(dx, dy);

        Direction[] dirs = Direction.values();
        Direction dir = dirs[random.ants().nextInt(dirs.length)];

        Cell spawnCell = cell.world().get(pos);
        AntParameters parameters = new AntParameters(antSpawnParameters);
        Ant ant = new Ant(this, parameters, spawnCell, dir, random.ants().nextLong());
        cell.world().track(ant);
        ants.add(ant);
        spawnCell.addAnt(ant);
        return ant;
    }

    /**
     * Removes food from the colony
     *
     * @param amount must be positive and less than the available food
     */
    public void decreaseFood(float amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be positive");
        if (amount > this.food) throw new IllegalArgumentException("amount must less than available");
        this.food -= amount;
    }

    /**
     * Adds food to the colony.
     *
     * @param amount must be positive
     */
    public void increaseFood(float amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be positive");
        this.food += amount;
    }

    /**
     * @return the amount of food that this colony has, always >= 0
     */
    @Contract(pure = true)
    public float food() {
        return food;
    }

    /**
     * Retrieves an unmodifiable view of the ants in the colony.
     *
     * @return An unmodifiable set of ants in the colony.
     */
    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Set<Ant> ants() {
        return Collections.unmodifiableSet(ants);
    }

    /**
     * Retrieves the ant spawning parameters for this colony.
     *
     * @return The ant spawning parameters.
     */
    @NotNull
    @Contract(pure = true)
    public AntParameters antSpawnParameters() {
        return antSpawnParameters;
    }

    /**
     * Sets the ant spawning parameters for this colony.
     *
     * @param antSpawnParameters The ant spawning parameters to set.
     * @throws NullPointerException if antSpawnParameters is null.
     */
    public void setAntSpawnParameters(@NotNull AntParameters antSpawnParameters) {
        Objects.requireNonNull(antSpawnParameters);
        this.antSpawnParameters = antSpawnParameters;
    }

    /**
     * Retrieves the parameters for this colony.
     *
     * @return The parameters for this colony.
     */
    @NotNull
    @Contract(pure = true)
    public Colony.Parameters parameters() {
        return parameters;
    }

    /**
     * Sets the parameters for this colony.
     *
     * @param parameters The parameters to set for this colony.
     * @throws NullPointerException if parameters is null.
     */
    public void setParameters(@NotNull Colony.Parameters parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
    }


    public void spread(int n) {
        Randoms random = cell.world().simulation().orElseThrow().randoms();
        for (int i = 0; i < n; i++) {
            Vector pos = new Vector(position());
            Direction dir = Direction.North.right(random.places().nextInt(8));
            while (world.get(pos).colony() != null) {
                pos.add(dir);
                dir = dir.left(random.places().nextInt(3) - 1);
            }
            Cell cell = world.get(pos);
            cell.setColony(this);
            cells.add(cell);
        }
    }

    /**
     * Retrieves an unmodifiable view of the cells occupied by this colony.
     *
     * @return An unmodifiable list of cells occupied by this colony.
     */
    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public List<Cell> cells() {
        return Collections.unmodifiableList(cells);
    }

    public static class Parameters {
        public float antSpawnFoodCost;
        public float antSpawnFoodThreshold;
    }
}
