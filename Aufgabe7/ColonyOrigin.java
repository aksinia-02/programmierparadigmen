public interface ColonyOrigin {

    /**
     * Checks if the colony origin supports a given form regulation.
     *
     * @param regulation Must not be null.
     * @return {@code true} if the colony origin supports the given form regulation, {@code false} otherwise.
     */
    boolean isCompatible(FormRegulation regulation);
}