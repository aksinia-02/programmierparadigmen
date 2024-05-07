/**
 * Represents the regulation status of a form.
 */
public interface FormRegulation {

    /**
     * Indicates whether the formicarium supports European ants.
     *
     * @return true if the formicarium supports European ants, false otherwise.
     */
    default boolean supportsEuropeanAnts() {
        return false;
    }

    /**
     * Indicates whether the formicarium supports tropical ants.
     *
     * @return true if the formicarium supports tropical ants, false otherwise.
     */
    default boolean supportsTropicalAnts() {
        return false;
    }

}
