/**
 * Represents an ant colony with specific attributes such as origin and size.
 */
public class Colony {

    // Origin of the ant colony (Europe, Tropics), not null
    private final ColonyOrigin origin;

    // Size of the ant colony (Large, Medium, Small), not null
    private final ColonySize size;

    // The assigned Formicarium to the colony (nullable if unassigned)
    private Form form;

    /**
     * Creates an ant colony with the specified origin and size.
     *
     * @param origin Must not be null
     * @param size   Must not be null
     */
    public Colony(ColonyOrigin origin, ColonySize size) {
        if (origin == null) Util.panic("origin is null");
        if (size == null) Util.panic("size is null");
        this.origin = origin;
        this.size = size;
    }

    /**
     * Retrieves the formicarium of the colony.
     *
     * @return The {@code Form} of the colony.
     */
    public Form form() {
        return form;
    }

    /**
     * Checks if the provided {@code Form} is preferred by this colony.
     *
     * @param form The form to check for preference.
     * @return {@code true} if the form is preferred based on its ize, {@code false} otherwise.
     * <h4>Contract</h4>
     * <ul>
     * <li> Assumes that the {@code Form} and its attributes (size and type) are not null.
     * <li> Delegates the size-based support check to the {@code FormSize} interface.
     * <li> The method returns {@code true} if the size is preferred by the colony, otherwise {@code false}.
     * </ul>
     */
    public boolean prefers(Form form) {
        if(form == null) Util.panic("form is null");
        return form.preferredBy(size);
    }

    /**
     * Assigns the provided form to the colony if it is not null and not already occupied.
     *
     * @param form The form to be assigned to the colony.
     *             If null or already occupied, an error is produced.
     */
    public void assign(Form form) {
        if (form == null) Util.panic("form is null");
        if (form.colony() == null) Util.panic("assign in form must be called first");
        if (form.colony() != this) Util.panic("form is occupied by a different colony");
        if (this.form != null) Util.panic("a form is already assigned");
        if (!form.supports(this)) Util.panic("cannot be supported by form");
        this.form = form;
    }

    /**
     * Unassigns any currently assigned form from the colony, if it is not null.
     * The colony will have no associated form after calling this method.
     */
    public void unassign() {
        if (this.form == null) Util.panic("no form is assigned");
        if (this.form.colony() == this) Util.panic("unassign in form must be called first");
        this.form = null;
    }

    @Override
    public String toString() {
        return "Colony{" +
            "origin=" + origin +
            ", size=" + size +
            '}';
    }

    /**
     * @param size Must not be null
     */
    public boolean isCompatible(FormSize size) {
        if(size == null) Util.panic("size is null");
        return this.size.isCompatible(size);
    }

    /**
     * @param regulation Must not be null
     */
    public boolean isCompatible(FormRegulation regulation) {
        if(regulation == null) Util.panic("regulation is null");
        return this.origin.isCompatible(regulation);
    }
}
