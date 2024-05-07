package aufgabe5;

import org.jetbrains.annotations.NotNull;

public interface Rated<P, R> {

    /**
     * Returns the rating of this instance based on the specified criteria.
     *
     * @param p The criteria parameter. Must not be null.
     * @return The calculated rating.
     */
    R rated(@NotNull P p);

    /**
     * Sets the criterion for future calls to {@link #rated()}.
     *
     * @param p The criterion to set. Must not be null.
     */
    void setCriterion(P p);

    /**
     * Calls {@link #rated(P)} with the criterion set by {@link #setCriterion(P)}.
     *
     * @return The rating based on the previously set criterion.
     */
    R rated();
}
