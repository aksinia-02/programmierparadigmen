public class FormSizeSmall implements FormSize {

    @Override
    public boolean preferredBy(ColonySize size) {
        return size.prefersSmallForm();
    }

    @Override
    public boolean supportsSmallColony() {
        return true;
    }

    @Override
    public String toString() {
        return "small";
    }
}
