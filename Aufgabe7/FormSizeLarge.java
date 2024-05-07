public class FormSizeLarge implements FormSize {

    @Override
    public boolean preferredBy(ColonySize size) {
        return size.prefersLargeForm();
    }

    @Override
    public boolean supportsMediumColony() {
        return true;
    }

    @Override
    public boolean supportsLargeColony() {
        return true;
    }

    @Override
    public String toString() {
        return "large";
    }
}
