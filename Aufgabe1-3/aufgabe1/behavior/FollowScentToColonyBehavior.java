package aufgabe1.behavior;

import aufgabe1.world.Cell;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.Colony;
import org.jetbrains.annotations.NotNull;

/**
 * Controls the follow-scent-to-colony behavior of an ant.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class FollowScentToColonyBehavior extends Behavior {

    private int noColonyScentSteps;

    protected FollowScentToColonyBehavior(Behaviors behaviors) {
        super(behaviors);
    }

    /**
     * Calculates the weighted direction for a cell based on colony scent while avoiding 'avoid' scent
     */
    @Override
    @NotNull
    protected Score evaluateDirectionScore(@NotNull WeightedDirection direction) {
        Colony colony = behaviors.ant.colony();
        if (direction.cell().colony() == colony) {
            return new Score(1, 0);
        }

        return new Score(mixScents(direction.cell().avoidScent(colony), direction.cell().colonyScent(colony)));
    }

    /**
     * Called when the behavior changes to this one
     */
    @Override
    public void begin() {
        super.begin();
        noColonyScentSteps = 0;
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
        if (bestNextDirection.cell().colonyScent(ant.colony()) < 0.01) {
            noColonyScentSteps++;
        } else {
            noColonyScentSteps = 0;
        }

        if (cell.colony() == ant.colony()) {
            ant.depositAllFood(cell.colony());
            turnAround();
            ant.setNextBehavior(ant.behaviors().followScentToFood());
        } else if (cell.hasFood()) {
            assert cell.foodSource() != null;
            takeFoodAndReturn(cell.foodSource());
        }

        // TODO: parameterize
        boolean lost = noColonyScentSteps > 50;
        if (cell.world().isNight() && lost) {
            ant.setNextBehavior(behaviors.returnHome());
        }

        ant.emitFoodScent();
        ant.emitAvoidScent();
    }
}
