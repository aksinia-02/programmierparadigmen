/**
 * Represents a medium colony size preference.
 */
public class ColonySizeMedium implements ColonySize {

    @Override
    public boolean isCompatible(FormSize size) {
        return size.supportsMediumColony();
    }

    @Override
    public String toString() {
        return "medium";
    }

    @Override
    public boolean prefersMediumFrom() {
        return true;
    }
}
