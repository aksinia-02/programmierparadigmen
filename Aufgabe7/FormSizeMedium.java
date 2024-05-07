public class FormSizeMedium implements FormSize {

    @Override
    public boolean preferredBy(ColonySize size) {
        return size.prefersMediumFrom();
    }

    @Override
    public boolean supportsSmallColony() {
        return true;
    }

    @Override
    public boolean supportsMediumColony() {
        return true;
    }

    @Override
    public String toString() {
        return "medium";
    }
}
