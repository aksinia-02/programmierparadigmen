package aufgabe5;

public interface Calc<R> {

    /**
     * Returns the sum of this value and the specified other value.
     *
     * @param other The value to be added.
     * @return The sum of this value and the other value.
     */
    R sum(R other);

    /**
     * Returns the result of dividing this value by the specified denominator.
     *
     * @param denominator The value by which to divide.
     * @return The result of the division.
     */
    R ratio(int denominator);

    /**
     * Checks if this value is greater than or equal to the specified other value.
     *
     * @param other The value to compare against.
     * @return True if this value is greater than or equal to the other value; false otherwise.
     */
    boolean atLeast(R other);
}
