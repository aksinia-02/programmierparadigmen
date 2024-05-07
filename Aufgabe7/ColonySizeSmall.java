/**
 * Represents a small colony size preference.
 */
public class ColonySizeSmall implements ColonySize {

    @Override
    public boolean isCompatible(FormSize size) {
        return size.supportsSmallColony();
    }

    @Override
    public String toString() {
        return "small";
    }

    @Override
    public boolean prefersSmallForm() {
        return true;
    }
}
