/**
 * Represents the size preferences of a colony regarding Formicaria.
 */
public interface ColonySize {

    /**
     * Determines the compatability of the colony for a given FormSize.
     *
     * @param size Must not be null
     * @return true if the colony is compatibly with the given size.
     */
    boolean isCompatible(FormSize size);

    /**
     * Indicates whether the colony prefers a small form size.
     *
     * @return true if the colony prefers a small form size, false otherwise.
     */
    default boolean prefersSmallForm() {
        return false;
    }

    /**
     * Indicates whether the colony prefers a medium form size.
     *
     * @return true if the colony prefers a medium form size, false otherwise.
     */
    default boolean prefersMediumFrom() {
        return false;
    }

    /**
     * Indicates whether the colony prefers a large form size.
     *
     * @return true if the colony prefers a large form size, false otherwise.
     */
    default boolean prefersLargeForm() {
        return false;
    }
}
