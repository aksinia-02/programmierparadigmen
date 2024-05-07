package aufgabe5;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;

/**
 * A class representing numeric values.
 */
public class Numeric implements Calc<Numeric>, Rated<DoubleUnaryOperator, Numeric>, DoubleUnaryOperator {
    private final double value;
    private DoubleUnaryOperator criterion;

    public Numeric(double value) {
        this.value = value;
    }

    public double value(){
        return value;
    }

    /**
     * Returns the sum of this numeric value and another numeric value.
     *
     * @param other The other numeric value.
     * @return The sum of this and the other numeric value.
     */
    @Override
    public Numeric sum(Numeric other) {
        return new Numeric(value + other.value);
    }

    /**
     * Returns the result of the division of this numeric value by the specified denominator.
     *
     * @param denominator The denominator for the division.
     * @return The result of the division.
     */
    @Override
    public Numeric ratio(int denominator) {
        return new Numeric(value / denominator);
    }

    /**
     * Returns true if this numeric value is greater or equal to another numeric value.
     *
     * @param other The other numeric value.
     * @return True if this is greater or equal to the other numeric value, false otherwise.
     */
    @Override
    public boolean atLeast(Numeric other) {
        return value >= other.value;
    }

    /**
     * Sets the criterion for rating this numeric value.
     *
     * @param criterion The criterion to set.
     */
    @Override
    public void setCriterion(DoubleUnaryOperator criterion) {
        this.criterion = criterion;
    }

    /**
     * Returns the rating of this numeric value according to the set criterion.
     * If no criterion is set, returns the current numeric value.
     *
     * @return The rated numeric value.
     */
    @Override
    public Numeric rated() {
        if (criterion == null) return this;
        return rated(criterion);
    }

    /**
     * Returns the rating of this numeric value by the specified criterion.
     *
     * @param p The criterion for rating.
     * @return The rated numeric value.
     */
    @Override
    public Numeric rated(@NotNull DoubleUnaryOperator p) {
        return new Numeric(p.applyAsDouble(value));
    }

    /**
     * Applies the numeric value as a double.
     *
     * @param operand The operand to apply the numeric value to.
     * @return The result of applying the numeric value.
     */
    @Override
    public double applyAsDouble(double operand) {
        return operand;
    }

}
