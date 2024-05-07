/**
 * Represents a regulated form.
 */
public class FormRegulationRegulated implements FormRegulation {

    @Override
    public boolean supportsTropicalAnts() {
        return true;
    }

    @Override
    public String toString() {
        return "regulated";
    }
}
