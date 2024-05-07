/**
 * Represents an unregulated form.
 */
public class FormRegulationUnregulated implements FormRegulation {

    @Override
    public boolean supportsEuropeanAnts() {
        return true;
    }

    @Override
    public String toString() {
        return "unregulated";
    }
}
