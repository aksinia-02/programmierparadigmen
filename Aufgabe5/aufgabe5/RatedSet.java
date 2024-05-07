package aufgabe5;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * An interface representing a set of rated elements.
 * Elements can be added to the set, criteria can be added, and iterators are provided for various conditions.
 *
 * @param <X> The type of elements in the set.
 * @param <P> The type of criteria for rating elements.
 * @param <R> The type of rating results.
 */
public interface RatedSet<X extends Rated<? super P, R>, P, R extends Calc<R>> extends Iterable<X> {

    /**
     * Adds an element to the set, unless it is already contained.
     *
     * @param x The element to add. Must not be null.
     */
    void add(X x);

    /**
     * Adds a criterion to the set, regardless of whether it is already contained.
     *
     * @param p The criterion to add. Must not be null.
     */
    void addCriterion(P p);

    /**
     * Returns an iterator over all elements of type X.
     * The Iterator can be used to remove elements.
     *
     * @return An iterator over the elements.
     */
    @Override
    @NotNull
    Iterator<X> iterator();

    /**
     * Returns an iterator over all elements of type X,
     * where {@code x.rated(p).atLeast(r)} is true.
     * The Iterator can be used to remove elements.
     *
     * @param p The criterion for filtering elements.
     * @param r The minimum rating for filtering elements.
     * @return An iterator over the filtered elements.
     */
    Iterator<X> iterator(P p, R r);

    /**
     * Returns an iterator over all elements of type X,
     * where the average of {@code x.rated(p)} with all criterions as {@code p} is at least {@code r}.
     * May throw an IllegalStateException if no average can be determined.
     * The Iterator can be used to remove elements.
     *
     * @param r The minimum average rating for filtering elements.
     * @return An iterator over the filtered elements.
     */
    Iterator<X> iterator(R r);

    /**
     * Returns an iterator over all criterions.
     * The Iterator can be used to remove elements.
     *
     * @return An iterator over the criterions.
     */
    Iterator<P> criterions();
}
