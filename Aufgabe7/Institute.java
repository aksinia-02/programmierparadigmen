import java.util.ArrayList;
import java.util.List;


/**
 * Represents an institute managing Formicaria, their inventory, and assignments to colonies.
 */
public class Institute {

    // List of free forms in the inventory
    private final List<Form> inventory = new ArrayList<>();

    // List of occupied forms in the institute
    private final List<Form> occupied = new ArrayList<>();

    /**
     * Adds the provided {@code Form} to the inventory
     * if it is not null, is free and not yet in the inventory.
     *
     * @param form The form to be added to the inventory.
     */
    public void addForm(Form form) {
        if (form == null) Util.panic("form is null");
        if (!form.isFree()) Util.panic("form is occupied");
        if (inventory.contains(form)) Util.panic("form already in inventory");
        inventory.add(form);
    }

    /**
     * Deletes the provided {@code Form} from the inventory if
     * it is not null, is free and in the inventory
     *
     * @param form The form to be deleted from the inventory.
     */
    public void deleteForm(Form form) {
        if (form == null) Util.panic("form is null");
        if (!form.isFree()) Util.panic("form is occupied");
        if (!inventory.contains(form)) Util.panic("form not in inventory");
        inventory.remove(form);
    }

    /**
     * Attempts to assign a {@code Form} to the provided {@code Colony} based on
     * colony size preferences and form availability.
     *
     * @param colony must not be null
     * @return The assigned form if successful, or null if no suitable form is available.
     */
    public Form assignForm(Colony colony) {
        if (colony == null) Util.panic("colony is null");
        for (Form form : inventory) {
            if (form.supports(colony) && colony.prefers(form)) {
                inventory.remove(form);
                occupied.add(form);
                return form;
            }
        }
        for (Form form : inventory) {
            if (form.supports(colony) && !colony.prefers(form)) {
                inventory.remove(form);
                occupied.add(form);
                return form;
            }
        }

        return null;
    }

    /**
     * Returns a {@code Form} to the inventory,
     * unassigning it from any colony, if it is not null, not free and in the occupied list.
     *
     * @param form Must not be null
     */
    public void returnForm(Form form) {
        if (form == null) Util.panic("form is null");
        if (form.isFree()) Util.panic("form is free");
        if (!occupied.contains(form)) Util.panic("form is not in occupied list");
        occupied.remove(form);
        inventory.add(form);
        form.unassign();
    }

    /**
     * Displays the total price of all free forms in the inventory on the screen.
     * Calculates the sum of prices for each free formicarium and prints the result.
     */
    public void priceFree() {
        float sum = 0;
        for (Form form : inventory) {
            sum += form.price();
        }
        System.out.printf("Total price of %d free forms: %.2f\n", inventory.size(), sum);
    }

    /**
     * Displays the total price of all occupied forms in the institute on the screen.
     * Calculates the sum of prices for each occupied formicarium and prints the result.
     */
    public void priceOccupied() {
        float sum = 0;
        for (Form form : occupied) {
            sum += form.price();
        }
        System.out.printf("Total price of %d occupied forms: %.2f\n", occupied.size(), sum);
    }

    /**
     * Displays information about all formicaria in the inventory on the screen.
     * Prints the string representation of each formicarium.
     */
    public void showFormicarium() {
        for (Form form : inventory) {
            System.out.println(form.toString());
        }
    }

    /**
     * Displays information about all occupied formicaria in the institute on the screen.
     * Prints the colony residing in each occupied formicarium along with formicarium details.
     */
    public void showAnts() {
        for (Form form : occupied) {
            System.out.printf("%s resides in %s\n", form.colony(), form);
        }
    }

    /**
     * Gets the size of the inventory.
     *
     * @return The number of forms in the inventory.
     */
    public int sizeOfInventory() {
        return inventory.size();
    }

    /**
     * Gets the size of the occupied forms.
     *
     * @return The number of forms that are currently occupied.
     */
    public int sizeOfOccupied() {
        return occupied.size();
    }

}
