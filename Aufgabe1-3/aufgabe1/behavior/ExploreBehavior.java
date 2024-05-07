package aufgabe1.behavior;

import aufgabe1.world.Cell;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.Colony;
import org.jetbrains.annotations.NotNull;

/**
 * Controls the explore-behavior of an ant.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class ExploreBehavior extends Behavior {

    private boolean wasOnHighScent = false;

    protected ExploreBehavior(Behaviors behaviors) {
        super(behaviors);
    }

    /**
     * Evaluates the score for a given direction based on factors such as food scent, colony scent, and height difference.
     *
     * @param direction The weighted direction.
     * @return A Score object indicating the calculated score for the direction.
     */
    @Override
    @NotNull
    protected Score evaluateDirectionScore(@NotNull WeightedDirection direction) {
        Colony colony = behaviors.ant.colony();
        Cell cell = direction.cell();
        if (cell.foodScent(colony) >= behaviors.parameters().highScentThreshold) {
            return new Score(1, mixScents(0, cell.foodScent(colony)));
        }
        // Calculate height difference between the current cell and the ant's cell
        float heightDifference = Math.max(cell.height() - behaviors.ant.cell().height(), 0) * behaviors.parameters().weightHeight;
        // Determine the maximum scent value between colony scent and food scent
        float maxScent = Math.max(cell.colonyScent(colony), cell.foodScent(colony));
        // Combine avoid scent, height difference, and inverse of max scent using the mixScents function
        return new Score(mixScents(cell.avoidScent(colony) + heightDifference, -1 * maxScent));
    }

    /**
     * Defines the behavior of an ant during its turn.
     */
    public void act() {
        super.act();

        Ant ant = behaviors.ant;
        Cell cell = behaviors.ant.cell();

        boolean onHighScent = isHighColonyScent(ant.cell().colonyScent(ant.colony()));

        WeightedDirection bestNextDirection = evaluateBestDirection();
        ant.setNextDirection(bestNextDirection.direction());

        if (cell.hasFood()) {
            assert cell.foodSource() != null;
            takeFoodAndReturn(cell.foodSource());
        } else if (isNearHighFoodScent(ant)) {
            ant.setNextBehavior(ant.behaviors().followScentToFood());
        }

        // stop exploring at night
        if (ant.cell().world().isNight()) {
            ant.setNextBehavior(ant.behaviors().returnHome());
        }

        boolean highScentNearby = highColonyScentNearby(ant) || wasOnHighScent;
        if (!onHighScent && !highScentNearby) {
            ant.emitColonyScent();
        }

        wasOnHighScent = onHighScent;
        ant.emitAvoidScent();
    }

    /**
     * Checks if the provided colony scent intensity is considered high
     * based on the predefined high scent threshold in the behaviors parameters.
     *
     * @param scent The colony scent intensity to be checked.
     * @return True if the scent is equal to or exceeds the high scent threshold; otherwise, false.
     */
    private boolean isHighColonyScent(float scent) {
        return scent >= behaviors.parameters().highScentThreshold;
    }

    /**
     * Checks if there is a high concentration of food scent nearby and if there are ants from the same colony carrying food.
     *
     * @param ant The ant for which to check the surroundings.
     * @return true if there is a high food scent or ants from the same colony carrying food nearby, false otherwise.
     * Precondition: ant != null.
     */
    private boolean isNearHighFoodScent(@NotNull Ant ant) {
        for (WeightedDirection direction : ant.possibleNextCells()) {
            Cell cell = direction.cell();
            if (cell.foodScent(ant.colony()) >= behaviors.parameters().highScentThreshold) {
                return true;
            }
            for (Ant other : cell.ants()) {
                if (other == ant) continue;
                if (other.isCarrying() && ant.colony().equals(other.colony())) return true;
            }
        }

        return false;
    }

    /**
     * Checks if there is a cell with a high colony scent nearby the given ant.
     *
     * @param ant The ant for which to check the surrounding cells.
     * @return True if a cell with high colony scent is nearby, otherwise false.
     * Precondition: ant != null.
     */
    private boolean highColonyScentNearby(@NotNull Ant ant) {
        for (WeightedDirection direction : ant.possibleNextCells()) {
            Cell cell = direction.cell();
            if (isHighColonyScent(cell.colonyScent(ant.colony()))) {
                return true;
            }
        }

        return false;
    }
}
