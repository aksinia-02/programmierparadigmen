package aufgabe1;

public interface IVector {
    /**
     * @return returns the Chebyshev length of this vector
     */
    default int length() {
        return Math.max(Math.abs(x()), Math.abs(y()));
    }

    int x();

    int y();

    /**
     * @return true if the x and y values are 0
     */
    default boolean isZero() {
        return x() == 0 && y() == 0;
    }
}
