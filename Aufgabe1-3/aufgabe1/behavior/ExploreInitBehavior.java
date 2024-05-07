package aufgabe1.behavior;

import aufgabe1.world.Cell;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.Colony;
import org.jetbrains.annotations.NotNull;

public class ExploreInitBehavior extends Behavior {

    private int steps;


    protected ExploreInitBehavior(Behaviors behaviors) {
        super(behaviors);
    }

    /**
     * Evaluates the score for a given direction based on the colony scent in the cell.
     *
     * @param direction The weighted direction to be evaluated.
     * @return A Score object representing the calculated score for the direction.
     */
    @Override
    @NotNull
    protected Score evaluateDirectionScore(@NotNull WeightedDirection direction) {
        Colony colony = behaviors.ant.colony();
        Cell cell = direction.cell();
        return new Score(mixScents(0, 2 * cell.colonyScent(colony)));
    }

    /**
     * Called when the behavior changes to this one
     * Generates a random number of steps (duration) for the behavior to run, based on a Gaussian distribution.
     * The generated steps are parameterized with a mean of 20 and a standard deviation of 50.
     */
    @Override
    public void begin() {
        super.begin();
        // TODO: parameterize
        steps = (int) (behaviors.random.nextGaussian() * 50 + 20);
    }

    /**
     * Called when the behavior changes to a different one
     */
    @Override
    public void act() {
        super.act();

        Ant ant = behaviors.ant;

        float scentBelow = ant.cell().colonyScent(ant.colony());
        boolean onHighScent = scentBelow >= behaviors.parameters.highScentThreshold;
        boolean onVeryHighScent = scentBelow >= behaviors.parameters.highScentThreshold * 2;

        WeightedDirection best = evaluateBestDirection();
        ant.setNextDirection(best.direction());
        float scentNext = best.cell().colonyScent(ant.colony());
        boolean nextHighScent = scentNext >= behaviors.parameters.highScentThreshold;

        // strengthen existing trails
        if (onVeryHighScent) {
            ant.emitColonyScent();
        }

        boolean endOfTrail = !onHighScent && !nextHighScent;
        if (ant.cell().colony() == ant.colony()) {
            endOfTrail = false;
        }

        if (steps <= 0 || endOfTrail) {
            ant.setNextBehaviorImmediate(behaviors.explore());
            return;
        }
        steps--;
    }
}
