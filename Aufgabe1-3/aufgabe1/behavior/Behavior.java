package aufgabe1.behavior;

import aufgabe1.Direction;
import aufgabe1.world.Cell;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.FoodSource;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents one node in a finite state machine.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public abstract class Behavior {
    protected final Behaviors behaviors;
    private int noReturnTimer;

    protected Behavior(Behaviors behaviors) {
        this.behaviors = behaviors;
    }

    /**
     * Called when the behavior changes to this one
     */
    public void begin() {
        noReturnTimer = 0;
    }

    /**
     * Called when the behavior changes to a different one
     */
    public void end() {
    }

    /**
     * Defines the behavior of an ant during its turn.
     */
    public void act() {
        if (noReturnTimer > 0) noReturnTimer--;
    }

    /**
     * Mixes avoidScent with pursueScent based on predefined parameters.
     *
     * @param avoidScent  The intensity of the scent to avoid.
     * @param pursueScent The intensity of the scent to pursue.
     * @return The result of mixing the scents, considering weights and bias, processed through a sigmoid activation function.
     */
    protected float mixScents(float avoidScent, float pursueScent) {
        float sum = avoidScent * behaviors.parameters().weightAvoid + pursueScent * behaviors.parameters().weightPursue + behaviors.parameters().biasMix;
        return sigmoid(sum);
    }

    /**
     * Calculates the sigmoid function for a given input.
     * The sigmoid function is commonly used in machine learning and neural networks.
     *
     * @param x The input value for which the sigmoid function is calculated.
     * @return The result of the sigmoid function applied to the input.
     */
    protected static float sigmoid(float x) {
        return (float) (1.0f / (1.0f + Math.exp(-x)));
    }

    /**
     * Takes food from a specified food source, initiates a turn-around action,
     * and sets the next behavior for the ant to follow the scent trail back to the nest.
     *
     * @param foodSource The food source from which the ant picking up food.
     */
    //BAD: low class connection(Behavior and FoodSource), strong object coupling
    //To reduce the low class connectivity and unwanted dependency, the takeFoodAndReturn
    // method could be made abstract in the Behavior class, and the concrete implementation could
    // be done in the subclasses. This would relax the dependency on the concrete FoodSource class and
    // increase the flexibility of the code.
    protected void takeFoodAndReturn(@NotNull FoodSource foodSource) {
        Ant ant = behaviors.ant;
        ant.takeAllFood(foodSource);
        turnAround();
        ant.setNextBehavior(ant.behaviors().followScentToNest());
    }


    /**
     * Performs a turn-around action for the ant, setting the next direction to return to the nest.
     * If the 'noReturnTimer' is not expired, the ant's direction remains unchanged.
     */
    protected void turnAround() {
        Ant ant = behaviors.ant;
        if (noReturnTimer <= 0) {
            ant.setNextDirection(ant.returnDirection());
        }
        noReturnTimer += 2;
    }

    /**
     * Weighs all options, considers a random bias and returns the best one
     *
     * @return The best-weighted direction as a WeightedDirection object.
     */
    @NotNull
    protected WeightedDirection evaluateBestDirection() {
        WeightedDirection best = null;
        Float bestWeight = null;
        Integer bestSegment = null;
        for (WeightedDirection option : behaviors.ant.possibleNextCells()) {
            Score score = evaluateDirectionScore(option);
            if (bestSegment == null || score.segment > bestSegment) {
                bestSegment = score.segment;
                bestWeight = null;
                best = null;
            } else if (score.segment < bestSegment) {
                continue;
            }

            float sum = score.value * behaviors.parameters.weightScore
                + option.bias * behaviors.parameters.weightStraight
                + behaviors.parameters.biasChoose;

            float weight = sigmoid(sum);
            float randomBias = (float) behaviors.random.nextGaussian();
            float sum2 = behaviors.parameters().argRandom * sum * sum;
            randomBias *= behaviors.parameters().weightRandom * (1 - (sum2 / (sum2 + 1)));
            weight += randomBias;

            // Take maximum
            if (bestWeight == null || weight > bestWeight) {
                best = option;
                bestWeight = weight;
            }
        }
        // Should be impossible
        Objects.requireNonNull(best);
        return best;
    }

    /**
     * Abstract method to be implemented by subclasses for evaluating the weighted direction score for a given direction.
     *
     * @param direction The weighted direction.
     * @return A Score object indicating the calculated score for the direction.
     */
    protected abstract Score evaluateDirectionScore(@NotNull WeightedDirection direction);

    /**
     * Represents a score for a direction, including a segment identifier and a numerical value.
     * Instances of this class are used to convey the evaluation results of direction scores.
     */
    protected record Score(int segment, float value) {
        public Score(float score) {
            this(0, score);
        }
    }

    /**
     * Represents a weighted direction associated with a cell, a direction, and a bias.
     *
     * @param cell      The cell associated with the weighted direction.
     * @param direction The direction associated with the weighted direction.
     * @param bias      The bias value associated with the direction.
     */
    //GOOD: Using Records for WeightedDirection and Score simplifies the representation and manipulation of data and promotes clarity in code
    public record WeightedDirection(
        @NotNull
        Cell cell,
        @NotNull
        Direction direction,
        float bias
    ) {
        @NotNull
        public static WeightedDirection inDirection(@NotNull Cell start, @NotNull Direction direction, float bias) {
            return new WeightedDirection(start.neighbor(direction), direction, bias);
        }
    }
}
