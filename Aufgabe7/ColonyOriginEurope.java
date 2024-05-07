/**
 * Represents a colony origin in Europe, which unconditionally supports all form regulations.
 */
public class ColonyOriginEurope implements ColonyOrigin {
    @Override
    public boolean isCompatible(FormRegulation regulation) {
        return regulation.supportsEuropeanAnts();
    }

    @Override
    public String toString() {
        return "european";
    }
}
