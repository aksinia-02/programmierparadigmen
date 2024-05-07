/**
 * Represents a Formicarium with specific attributes such as size, regulation type, and price.
 * Tracks its current occupation status by a Colony.
 */
public class Form {

    // Size of the Formicarium (small, medium, large), not null
    private final FormSize size;

    // Regulation type of the Formicarium (regulated, non-regulated), not null
    private final FormRegulation regulation;

    // Price of the Formicarium
    private final float price;

    // The current colony occupying the Formicarium (nullable if unoccupied)
    private Colony colony;

    /**
     * Creates a Formicarium with the specified size, regulation type, and price.
     *
     * @param size       The size of the Formicarium (small, medium, large). Not null
     * @param regulation The regulation type of the Formicarium (regulated, non-regulated). Not null
     * @param price      The price of the Formicarium.
     */
    public Form(FormSize size, FormRegulation regulation, float price) {
        if (size == null) Util.panic("size is null");
        if (regulation == null) Util.panic("regulation is null");
        this.size = size;
        this.regulation = regulation;
        this.price = price;
    }

    /**
     * Checks if the provided {@code Colony} is supported by this formicarium.
     *
     * @param colony The colony to check for support.
     * @return {@code true} if the form is supported based on its colony size and origin, {@code false} otherwise.
     * <h4>Contract</h4>
     * <ul>
     * <li> Assumes that the {@code Colony} and its attributes (size and type) are not null.
     * <li> Delegates the size-based support check to the {@code ColonySize} interface.
     * <li> Delegates the origin-based support check to the {@code ColonyOrigin} interface.
     * <li> The method returns {@code true} if both size and origin support the form, otherwise {@code false}.
     * </ul>
     */
    public boolean supports(Colony colony) {
        if(colony == null) Util.panic("colony is null");
        return colony.isCompatible(size) && colony.isCompatible(regulation);
    }

    /**
     * Checks if the Formicarium is currently unoccupied.
     *
     * @return true if the Formicarium is unoccupied, false otherwise.
     */
    public boolean isFree() {
        return colony == null;
    }

    /**
     * Assigns the Formicarium to a specified Colony.
     * <p>
     * <b>Precondition</b>: The Formicarium must be unoccupied, and the Formicarium's size and regulation type must be compatible
     * with the specified Colony.</p>
     *
     * @param colony The Colony to which the Formicarium is assigned.
     */
    public void assign(Colony colony) {
        if (colony == null)
            Util.panic("colony is null");
        if (colony.form() != null && colony.form() != this) Util.panic("colony is occupying a different form");
        this.colony = colony;
        colony.assign(this);
    }

    /**
     * Unassigns the Formicarium from its current Colony, if a colony is assigned.
     */
    public void unassign() {
        if (this.colony == null) Util.panic("no colony is assigned");
        Colony temp = colony;
        this.colony = null;
        temp.unassign();
    }

    /**
     * Gets the price of the Formicarium.
     *
     * @return The price of the Formicarium.
     */
    public float price() {
        return price;
    }

    /**
     * Gets the Colony currently occupying the Formicarium (nullable if unoccupied).
     *
     * @return The Colony currently occupying the Formicarium.
     */
    public Colony colony() {
        return colony;
    }

    @Override
    public String toString() {
        return "Form{" +
            "size=" + size +
            ", type=" + regulation +
            ", price=" + price +
            '}';
    }

    /**
     * @param size Must not be null
     */
    public boolean preferredBy(ColonySize size) {
        if(size == null) Util.panic("size is null");
        return this.size.preferredBy(size);
    }
}
