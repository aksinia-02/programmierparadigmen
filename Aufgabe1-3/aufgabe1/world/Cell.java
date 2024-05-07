package aufgabe1.world;

import aufgabe1.Direction;
import aufgabe1.IVector;
import aufgabe1.Vector;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.Colony;
import aufgabe1.world.entity.FoodSource;
import aufgabe1.world.entity.Scent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * The `Cell` class represents a cell within a 2D grid. It contains information about its coordinates (x, y),
 * type, a reference to a `FoodSource` (if applicable), a set of ants located on the cell,
 * and scent intensities related to food, colony, and avoidance. The class provides methods for managing ants
 * and updating scent intensities over time.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class Cell {
    @NotNull
    private final World world;
    @NotNull
    private final IVector position;
    @NotNull
    private final List<Ant> ants = new ArrayList<>();
    // there are few colonies so a list is probably faster than a WeakHashMap
    private final List<Map.Entry<Colony, Scent>> scents = new ArrayList<>();
    private float height;
    @Nullable
    private FoodSource foodSource;
    //BAD: high class connection, strong object coupling, because the object from the
    // Cell class contains a link to the object from the Colony class and uses the methods
    // from the Colony class
    //To reduce class connection, the Cell class could use interfaces or abstract classes to
    // relax the dependencies.
    @Nullable
    private Colony colony;

    public Cell(@NotNull World world, IVector position) {
        Objects.requireNonNull(world);
        this.world = world;
        this.position = new Vector(position);
    }

    /**
     * Gets the neighbouring cell in a specific direction.
     */
    @NotNull
    public Cell neighbor(@NotNull Direction dir) {
        return world.get(position.x() + dir.dx(), position.y() + dir.dy());
    }

    /**
     * Adds scent intensity related to food.
     */
    public void addFoodScent(float strength, @NotNull Colony colony) {
        wake();
        getScent(colony).food += strength;
    }

    public void wake() {
        world.wake(position.x(), position.y());
    }

    /**
     * Gets the scent of a specified colony.
     */
    @NotNull
    public Scent getScent(@NotNull Colony colony) {
        Scent scent = getScentOrNull(colony);
        if (scent != null) return scent;

        scent = new Scent();
        scents.add(Map.entry(colony, scent));
        return scent;
    }

    @Nullable
    public Scent getScentOrNull(@NotNull Colony colony) {
        for (Map.Entry<Colony, Scent> entry : scents) {
            if (entry.getKey() == colony) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Adds scent intensity related to colony.
     */
    public void addColonyScent(float strength, @NotNull Colony colony) {
        wake();
        getScent(colony).colony += strength;
    }

    /**
     * Adds scent intensity related to avoiding certain areas.
     */
    public void addAvoidScent(float strength, @NotNull Colony colony) {
        wake();
        getScent(colony).avoid += strength;
    }

    /**
     * Update the scent intensities over time based on decay rates.
     */
    public void update() {
        for (int i = 0; i < scents.size(); i++) {
            Map.Entry<Colony, Scent> entry = scents.get(i);
            Scent scent = entry.getValue();
            scent.food *= world.parameters().foodDecay;
            scent.colony *= world.parameters().colonyDecay;
            scent.avoid *= world.parameters().avoidDecay;

            if (scent.isEmpty()) {
                scents.remove(i);
                i--;
            }
        }

        if (isEmpty()) {
            suspend();
        }
    }

    public boolean isEmpty() {
        // empty scents get removed, so no need to check them
        return scents.isEmpty() && ants.isEmpty() && colony == null && foodSource == null;
    }

    public void suspend() {
        world.suspend(position.x(), position.y());
    }

    /**
     * @return true if the cell has any food
     */
    @Contract(pure = true)
    public boolean hasFood() {
        return foodSource != null && foodSource.amount() > 0;
    }

    @UnmodifiableView
    @NotNull
    public List<Ant> ants() {
        return Collections.unmodifiableList(ants);
    }

    @Contract(pure = true)
    public @Nullable FoodSource foodSource() {
        return foodSource;
    }

    public float foodScent(@NotNull Colony colony) {
        Scent scent = getScentOrNull(colony);
        if (scent == null) return 0;
        return scent.food;
    }

    public float colonyScent(@NotNull Colony colony) {
        Scent scent = getScentOrNull(colony);
        if (scent == null) return 0;
        return scent.colony;
    }

    public float avoidScent(@NotNull Colony colony) {
        Scent scent = getScentOrNull(colony);
        if (scent == null) return 0;
        return scent.avoid;
    }

    public float height() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Contract(pure = true)
    public @Nullable Colony colony() {
        return colony;
    }

    public void setFoodSource(@Nullable FoodSource foodSource) {
        if (foodSource == null && this.foodSource != null) {
            world.untrack(this.foodSource);
        }
        this.foodSource = foodSource;
    }

    public void setColony(@Nullable Colony colony) {
        this.colony = colony;
    }

    @NotNull
    public IVector position() {
        return position;
    }

    @NotNull
    public World world() {
        return world;
    }

    @UnmodifiableView
    @NotNull
    public List<Map.Entry<Colony, Scent>> scents() {
        return Collections.unmodifiableList(scents);
    }

    /**
     * Removes an ant from the cell.
     */
    public void removeAnt(Ant ant) {
        ants.remove(ant);
    }

    /**
     * Adds an ant to the cell.
     */
    public void addAnt(Ant ant) {
        if (!ants.contains(ant)) {
            ants.add(ant);
        }
    }
}
