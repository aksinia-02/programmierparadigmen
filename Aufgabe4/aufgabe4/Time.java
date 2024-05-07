package aufgabe4;


/**
 * Enumeration representing different time intervals, including special case INFINITE.
 */
public enum Time {
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR,
    INFINITE;

    /**
     * Converts a Time enum value to its corresponding integer representation.
     *
     * Precondition: The parameter 'time' is not null.
     *
     * @param time The Time enum value to convert.
     * @return The integer representation of the Time enum value.
     */
    private static int intFromTime(Time time) {
        return time.ordinal();
    }

    /**
     * Compares two Time enum values based on their ordinal positions.
     *
     * Precondition: The parameter 'value2' is not null.
     *
     * @param value2 The Time enum value to compare to.
     * @return A negative integer, zero, or a positive integer if this Time is less than, equal to,
     *         or greater than the specified Time, respectively.
     */
    public int compareTime(Time value2) {
        return Integer.compare(intFromTime(this), intFromTime(value2));
    }
}
