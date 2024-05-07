/**
 * Represents a large colony size preference.
 */
public class ColonySizeLarge implements ColonySize {

    @Override
    public boolean isCompatible(FormSize size) {
        return size.supportsLargeColony();
    }

    @Override
    public String toString() {
        return "large";
    }

    @Override
    public boolean prefersLargeForm() {
        return true;
    }
}
