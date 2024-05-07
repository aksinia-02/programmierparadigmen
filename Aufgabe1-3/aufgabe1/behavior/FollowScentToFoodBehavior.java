package aufgabe1.behavior;

import aufgabe1.world.Cell;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.Colony;
import aufgabe1.world.entity.FoodSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Controls the follow-scent-to-food behavior of an ant.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class FollowScentToFoodBehavior extends Behavior {
    @Nullable
    private Integer badScentFollowStartTime;

    protected FollowScentToFoodBehavior(Behaviors behaviors) {
        super(behaviors);
    }

    /**
     * Evaluates the score for a given direction based on the presence of food in the cell.
     *
     * @param direction The weighted direction to be evaluated.
     * @return A Score object representing the calculated score for the direction.
     */
    @Override
    @NotNull
    protected Score evaluateDirectionScore(@NotNull WeightedDirection direction) {
        // Check if the cell contains food
        FoodSource food = direction.cell().foodSource();
        if (food != null && !food.isEmpty()) {
            // If food is present, use sigmoid function on the food amount as the score
            return new Score(1, sigmoid(food.amount()));
        }
        // If there is no food, calculate the score using avoidScent and foodScent in the cell
        Colony colony = behaviors.ant.colony();
        return new Score(mixScents(direction.cell().avoidScent(colony), direction.cell().foodScent(colony)));
    }

    /**
     * Called when the behavior changes to this one
     */
    @Override
    public void begin() {
        super.begin();
        badScentFollowStartTime = null;
    }

    /**
     * Defines the behavior of an ant during its turn.
     */
    public void act() {
        super.act();

        Ant ant = behaviors.ant;
        Cell cell = ant.cell();

        WeightedDirection bestNextDirection = evaluateBestDirection();
        ant.setNextDirection(bestNextDirection.direction());

        boolean followingBadScent = isFollowingBadScent(ant);

        if (cell.hasFood()) {
            assert cell.foodSource() != null;
            takeFoodAndReturn(cell.foodSource());
            followingBadScent = false;
        }

        if (cell.colony() == ant.colony()) {
            turnAround();
        }

        if (followingBadScent) {
            if (isFollowingBadScentTooLong(ant)) {
                ant.setNextBehavior(ant.behaviors().exploreInit());
            }
        } else {
            badScentFollowStartTime = null;
        }

        ant.emitColonyScent();
        ant.emitAvoidScent();
    }

    /**
     * Checks if the ant is following a bad scent trail, meaning that there are no cells nearby with high food scent.
     *
     * @param ant The ant for which to check the surroundings.
     * @return true if there is no high food scent in the vicinity, indicating a bad scent trail; false otherwise.
     * Precondition: ant != null.
     */
    private boolean isFollowingBadScent(@NotNull Ant ant) {
        for (WeightedDirection direction : ant.possibleNextCells()) {
            if (direction.cell().foodScent(ant.colony()) >= behaviors.parameters().highScentThreshold) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the ant is following a bad scent trail too long.
     *
     * @param ant The ant for which to check the surroundings.
     * @return true if there is no high food scent in the vicinity, indicating a bad scent trail; false otherwise.
     * Precondition: ant != null.
     */
    private boolean isFollowingBadScentTooLong(@NotNull Ant ant) {
        if (badScentFollowStartTime == null) {
            badScentFollowStartTime = ant.cell().world().time();
        }

        float badScentFollowTime = ant.cell().world().time() - badScentFollowStartTime;
        return badScentFollowTime >= behaviors.parameters().badScentFollowTimeThreshold;
    }
}
