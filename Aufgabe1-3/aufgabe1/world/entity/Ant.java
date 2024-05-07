package aufgabe1.world.entity;

import aufgabe1.Direction;
import aufgabe1.behavior.Behavior;
import aufgabe1.behavior.Behaviors;
import aufgabe1.world.Cell;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The `Ant` class represents an individual ant in the simulation. Ants have various behaviors and can move
 * between cells, follow scent trails, and carry food.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class Ant extends Entity {
    @NotNull
    private final Colony colony;
    @NotNull
    private final Behaviors behaviors;
    @NotNull
    // TODO: parameterize
    private final PathHistory pathHistory = new PathHistory(80, 7);
    @NotNull
    private final List<Behavior.WeightedDirection> possibleNextCells = Arrays.asList(new Behavior.WeightedDirection[5]);
    @NotNull
    private Behavior behavior;
    @NotNull
    private Direction direction;
    private float carrying;
    private float colonyScentStrength;
    private float foodScentStrength;
    @NotNull
    private AntParameters parameters;
    private int sleep;
    private int energy;
    private boolean recordPath = true;
    @NotNull
    private Direction nextDirection;
    @NotNull
    private Direction returnDirection;
    @NotNull
    private Behavior nextBehavior;

    public Ant(@NotNull Colony colony, @NotNull AntParameters parameters, @NotNull Cell cell, @NotNull Direction direction, long seed) {
        super(cell);
        Objects.requireNonNull(colony);
        Objects.requireNonNull(direction);
        //BAD: high class connection, strong object coupling, because the object from the
        // Ant class contains a link to the object from the Colony class and uses the methods
        // from the Colony class
        //To reduce class connection, the Ant class could use interfaces or abstract classes to
        // relax the dependencies.
        this.colony = colony;
        this.cell = cell;
        this.direction = direction;
        this.returnDirection = direction;
        this.nextDirection = direction;
        this.parameters = parameters;
        this.energy = parameters.energyGain;
        this.behaviors = new Behaviors(this, cell.world().simulation().orElseThrow().randoms().newRandom(seed), parameters.behavior());
        this.behavior = behaviors.exploreInit();
        this.nextBehavior = behavior;
        updatePossibleNextCells();
    }

    /**
     * Updates the list of possible next cells based on the current cell, direction, and behavior parameters.
     * Preconditions:
     * - The 'possibleNextCells' list must have a size of at least 5.
     * Post-conditions:
     * - The 'possibleNextCells' list is updated with WeightedDirection objects representing possible directions.
     */
    private void updatePossibleNextCells() {
        possibleNextCells.set(0, Behavior.WeightedDirection.inDirection(cell, direction, parameters.straightBias));
        possibleNextCells.set(1, Behavior.WeightedDirection.inDirection(cell, direction.left(1), 0f));
        possibleNextCells.set(2, Behavior.WeightedDirection.inDirection(cell, direction.right(1), 0f));
        possibleNextCells.set(3, Behavior.WeightedDirection.inDirection(cell, direction.left(2), 0f));
        possibleNextCells.set(4, Behavior.WeightedDirection.inDirection(cell, direction.right(2), 0f));
    }

    /**
     * Represents the actions of the ant within the simulation world, including carrying food, leaving a scent trail, and changing behavior.
     */
    public void update() {
        replenishScents();

        if (cell.colony() == colony) {
            pathHistory.reset();
            int missingEnergy = parameters.energyGain - energy;
            if (missingEnergy > 1) {
                float requiredFood = Math.min(colony.food(), missingEnergy / parameters.energyFoodFactor);
                colony.decreaseFood(requiredFood);
                energy += (int) Math.ceil(requiredFood * parameters.energyFoodFactor);
            }
        }

        //GOOD: Dynamic binding: The actual implementation is chosen at runtime
        behavior.act();

        if (behavior != nextBehavior) {
            behavior.end();
            behavior = nextBehavior;
            behavior.begin();
        }
        if (cell.neighbor(nextDirection).height() > 0.62) {
            if (cell.height() > 0.62) {
                nextDirection = direction;
            } else {
                nextDirection = returnDirection;
            }
        }
        returnDirection = nextDirection.opposite();
        direction = nextDirection;
        if (sleeping()) {
            sleep--;
        } else {
            cell.world().moveAnt(this, cell.neighbor(direction));
            if (recordPath) {
                pathHistory.push(direction);
            }
            energy--;
        }

        if (energy <= 0) {
            energy += (int) Math.ceil(carrying * parameters.energyFoodFactor);
            carrying = 0;
        }
        if (energy <= 0) {
            die();
            return;
        }

        updatePossibleNextCells();
    }

    /**
     * Replenishes scents for the ant based on its current cell
     */
    public void replenishScents() {
        if (cell.colony() == colony) {
            colonyScentStrength = parameters.colonyScentGain;
        }

        if (cell.hasFood()) {
            foodScentStrength = parameters.foodScentGain;
        }
    }

    /**
     * Checks if the ant is currently in a sleeping state.
     *
     * @return True if the ant is sleeping; otherwise, false.
     */
    @Contract(pure = true)
    public boolean sleeping() {
        return sleep > 0;
    }

    /**
     * Performs the necessary actions when an ant dies
     */
    private void die() {
        colony.removeAnt(this);
        world.untrack(this);
        cell.removeAnt(this);
    }

    /**
     * @return a list of cells where the ant can move to
     */
    @Contract(pure = true)
    @NotNull
    public List<Behavior.@NotNull WeightedDirection> possibleNextCells() {
        return possibleNextCells;
    }

    /**
     * @return true if the ant is carrying any food
     */
    @Contract(pure = true)
    public boolean isCarrying() {
        return carrying > 0;
    }

    /**
     * @return the amount of food that this ant is carrying. Always >= 0
     */
    @Contract(pure = true)
    public float carrying() {
        return carrying;
    }

    @Contract(pure = true)
    /**
     * @return The colony to which the ant belongs.
     */
    @NotNull
    public Colony colony() {
        return colony;
    }

    /**
     * @return The current direction of the ant.
     */
    @Contract(pure = true)
    @SuppressWarnings("unused")
    @NotNull
    public Direction direction() {
        return direction;
    }

    /**
     * @return The parameters associated with the ant.
     */
    @Contract(pure = true)
    @SuppressWarnings("unused")
    @NotNull
    public AntParameters parameters() {
        return parameters;
    }

    /**
     * Sets the parameters for the ant, updating both the general parameters and the behavior parameters.
     *
     * @param parameters The ant parameters to be set. Must not be null.
     * Preconditions: parameters != null
     * Post-conditions: The ant's parameters and behavior parameters are updated.
     */
    public void setParameters(@NotNull AntParameters parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
        this.behaviors.setParameters(parameters.behavior());
    }

    /**
     * Emits colony scent from the ant to the current cell, updates colonyScentStrength.
     * Colony scent strength is adjusted based on the ant's parameters.
     * This method has no effect if the ant is sleeping.
     * Post-conditions: The colony scent is emitted to the cell, and the ant's colony scent strength is updated.
     */
    public void emitColonyScent() {
        if (sleeping()) return;
        cell.addColonyScent(Math.min(parameters.colonyScentAddend, colonyScentStrength), colony);
        colonyScentStrength *= parameters.colonyScentDecay;
    }

    /**
     * Emits food scent from the ant to the current cell, updating foodScentStrength.
     * Food scent strength is adjusted based on the ant's parameters.
     * This method has no effect if the ant is sleeping.
     * Post-conditions: The food scent is emitted to the cell, and the ant's food scent strength is updated.
     */
    public void emitFoodScent() {
        if (sleeping()) return;
        cell.addFoodScent(Math.min(parameters.foodScentAddend, foodScentStrength), colony);
        foodScentStrength *= parameters.foodScentDecay;
        cell.addFoodScent(Math.min(parameters.foodScentAddend, foodScentStrength), colony);
    }

    /**
     * Emits avoid scent from the ant to the current cell.
     * This method has no effect if the ant is sleeping.
     *
     * Post-conditions: avoid scent is emitted to the cell, and the cell's avoid scent is updated.
     */
    public void emitAvoidScent() {
        if (sleeping()) return;
        float addend = parameters.avoidScentAdded;
        addend += cell.avoidScent(colony) * parameters.avoidScentFactor;
        cell.addAvoidScent(addend, colony);
    }

    /**
     * Takes food from a specified food source, considering the ant's carrying capacity,
     * and updates the ant's carried food and the remaining amount in the food source.
     *
     * @param foodSource The food source from which the ant picks up food.
     */
    public void takeAllFood(@NotNull FoodSource foodSource) {
        float amount = Math.min(availableCarryingCapacity(), foodSource.amount());
        increaseCarrying(amount);
        foodSource.decreaseAmount(amount);
    }

    /**
     * Calculates the available carrying capacity for the ant.
     *
     * @return the amount of food that the ant could carry more. Always >= 0
     */
    @Contract(pure = true)
    public float availableCarryingCapacity() {
        return Math.max(parameters.carryingCapacity - carrying, 0f);
    }

    /**
     * Increases the amount of food the ant is carrying.
     *
     * @param amount must be positive and <= the available carrying capacity
     */
    public void increaseCarrying(float amount) {
        if (amount < 0) throw new IllegalArgumentException("'amount' must be positive");
        if (amount > availableCarryingCapacity())
            throw new IllegalArgumentException("'amount' must be less than available");
        carrying += amount;
    }

    public void depositAllFood(@NotNull Colony colony) {
        colony.increaseFood(carrying);
        decreaseCarrying(carrying);
    }

    /**
     * Decreases the amount of food the ant is carrying by the specified amount.
     *
     * @param amount must be positive and <= the amount that the ant is currently carrying
     */
    public void decreaseCarrying(float amount) {
        if (amount < 0) throw new IllegalArgumentException("'amount' must be positive");
        if (amount > carrying) throw new IllegalArgumentException("'amount' must be less than available");
        carrying -= amount;
    }

    /**
     * Sets the next direction for the ant.
     *
     * @param direction The direction to set as the next direction.
     */
    public void setNextDirection(@NotNull Direction direction) {
        this.nextDirection = direction;
    }

    @Contract(pure = true)
    @NotNull
    public Direction returnDirection() {
        return returnDirection;
    }

    /**
     * Gets the behaviors associated with the ant.
     *
     * @return The behaviors of the ant.
     */
    @Contract(pure = true)
    @NotNull
    public Behaviors behaviors() {
        return behaviors;
    }

    /**
     * Sets the next behavior for the ant to perform.
     *
     * @param behavior The behavior to set as the next one.
     */
    public void setNextBehavior(@NotNull Behavior behavior) {
        nextBehavior = behavior;
    }

    /**
     * Sets the next behavior for the ant to perform immediately, ending the current behavior and starting the new one.
     *
     * @param behavior The behavior to set as the next one.
     */
    public void setNextBehaviorImmediate(@NotNull Behavior behavior) {
        this.nextBehavior = behavior;
        this.behavior.end();
        this.behavior = this.nextBehavior;
        this.behavior.begin();
    }

    /**
     * Sets the cell that the ant is currently occupying.
     *
     * @param cell The cell to set for the ant.
     */
    public void setCell(@NotNull Cell cell) {
        this.cell = cell;
    }

    /**
     * Gets the path history associated with the ant.
     *
     * @return The path history of the ant.
     */
    @Contract(pure = true)
    @NotNull
    public PathHistory pathHistory() {
        return pathHistory;
    }

    /**
     * Sets the sleep duration for the ant.
     *
     * @param sleep The sleep duration to set.
     */
    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public void setRecordPath(boolean record) {
        this.recordPath = record;
    }
}
