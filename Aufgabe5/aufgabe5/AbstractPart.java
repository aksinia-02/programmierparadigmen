package aufgabe5;

import org.jetbrains.annotations.NotNull;

/**
 * An abstract base class implementing the {@link Part} interface.
 * Subclasses are expected to provide specific functionality for rating and representing parts.
 */
public abstract class AbstractPart implements Part {

    // https://tuwel.tuwien.ac.at/mod/forum/discuss.php?d=400955#p929912
    // Quality of this part.
    protected final Quality quality;
    // The criterion for rating this part.
    private Part criterion;

    protected AbstractPart(Quality quality) {
        this.quality = quality;
    }

    /**
     * Sets the criterion for future calls to {@link #rated()}.
     *
     * @param part The part to set as the criterion. Can be null.
     */
    @Override
    public void setCriterion(Part part) {
        criterion = part;
    }

    /**
     * Calls {@link Rated#rated(Object)} with the criterion set by {@link Rated#setCriterion(Object)}.
     * If no criterion is set, returns the quality of this part.
     *
     * @return The quality rating of this part.
     */
    @Override
    public Quality rated() {
        if (criterion == null) return quality;
        return rated(criterion);
    }

    /**
     * Returns a non-null string representation of this part.
     *
     * @return The string representation of this part.
     */
    @Override
    @NotNull
    public String toString() {
        return getClass().getSimpleName();
    }
}
