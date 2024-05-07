/**
 * Represents the preferred relationship of a formicarium size with a colony size.
 */
public interface FormSize {

    /**
     * Checks if the colony is preferred for a specific ColonySize.
     *
     * @param size Must not be null.
     * @return true if the colony is preferred for the specified size, false otherwise.
     */
    boolean preferredBy(ColonySize size);

    /**
     * Indicates whether the colony supports small colony sizes.
     *
     * @return true if the colony supports small colony sizes, false otherwise.
     */
    default boolean supportsSmallColony() { return false; }

    /**
     * Indicates whether the colony supports medium colony sizes.
     *
     * @return true if the colony supports medium colony sizes, false otherwise.
     */
    default boolean supportsMediumColony() {
        return false;
    }

    /**
     * Indicates whether the colony supports large colony sizes.
     *
     * @return true if the colony supports large colony sizes, false otherwise.
     */
    default boolean supportsLargeColony() {
        return false;
    }
}
