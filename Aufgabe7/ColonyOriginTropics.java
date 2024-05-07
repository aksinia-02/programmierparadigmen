/**
 * Represents a colony origin in the Tropics, where support for form regulations depends on the regulation itself.
 */
public class ColonyOriginTropics implements ColonyOrigin {
    @Override
    public boolean isCompatible(FormRegulation regulation) {
        return regulation.supportsTropicalAnts();
    }

    @Override
    public String toString() {
        return "tropical";
    }
}
