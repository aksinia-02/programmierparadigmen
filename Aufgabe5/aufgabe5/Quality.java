package aufgabe5;

/**
 * An enumeration representing the quality levels.
 */
public enum Quality implements Calc<Quality> {
    UNUSABLE("nicht für den Einsatz geeignet"),
    HOBBY("für den Einsatz im Hobbybereich geeignet"),
    SEMIPROFESSIONAL("für den semiprofessionellen Einsatz geeignet"),
    PROFESSIONAL("für den professionellen Einsatz geeignet");


    private final String description;

    Quality(String description) {
        this.description = description;
    }

    /**
     * Returns the lower quality.
     *
     * @param other The quality to compare with.
     * @return The lower of the two qualities.
     */
    @Override
    public Quality sum(Quality other) {
        return ordinal() <= other.ordinal() ? this : other;
    }

    /**
     * Returns this quality.
     *
     * @param denominator The denominator for the ratio (not used).
     * @return This quality.
     */
    @Override
    public Quality ratio(int denominator) {
        return this;
    }

    /**
     * Returns true if this quality is greater or equal to the parameter.
     *
     * @param other The quality to compare with.
     * @return True if this quality is greater or equal to the parameter, false otherwise.
     */
    @Override
    public boolean atLeast(Quality other) {
        return ordinal() >= other.ordinal();
    }

    @Override
    public String toString() {
        return description;
    }
}
