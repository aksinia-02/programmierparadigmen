package aufgabe5;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A set that maintains statistics about its operations.
 *
 * @param <X> The type of elements in the set.
 * @param <P> The type of criterion.
 * @param <R> The type of rating.
 */
public class StatSet<X extends Rated<? super P, R>, P, R extends Calc<R>> extends AbstractRatedSet<X, P, R> {
    private final StringMap<Integer> statistics = new StringMap<>();

    public StatSet() {
        super();
        increment("StatSet#<init>()");
    }

    /**
     * Increments the count for the given method in the statistics map.
     *
     * @param method The method name for which the count should be incremented.
     */
    protected void increment(String method) {
        statistics.put("RatedSet#increment(String)", statistics.getOrDefault("StatSet#increment(String)", 0) + 1);
        statistics.put(method, statistics.getOrDefault(method, 0) + 1);
    }

    /**
     * Returns a string representation of the statistics.
     *
     * @return A string containing the statistics.
     */
    public String statistics() {
        increment("StatSet#statistics()");
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : statistics.entries()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Adds an element to the set, unless it is already contained.
     *
     * @param x The element to be added.
     */
    @Override
    public void add(X x) {
        increment("RatedSet#add(X)");
        super.add(x);
    }

    /**
     * Adds a criterion to the set, regardless of whether it is already contained.
     *
     * @param p The criterion to be added.
     */
    @Override
    public void addCriterion(P p) {
        increment("RatedSet#addCriterion(P)");
        super.addCriterion(p);
    }

    /**
     * Returns an iterator over all elements of type X.
     *
     * @return An iterator over all elements of type X.
     */
    @Override
    @NotNull
    public Iterator<X> iterator() {
        increment("RatedSet#iterator()");
        return new DelegateIterator<>(super.iterator());
    }

    /**
     * Returns an iterator over all elements of type X, where {@code x.rated(p).atLeast(r)} is true.
     *
     * @param p The criterion for filtering elements.
     * @param r The minimum rating for elements to be included in the iterator.
     * @return An iterator over filtered elements.
     */
    @Override
    public Iterator<X> iterator(P p, R r) {
        increment("RatedSet#iterator(P, R)");
        return new DelegateIterator<>(super.iterator(p, r));
    }

    /**
     * Returns an iterator over all elements of type X, where the average of {@code x.rated(p)} with all
     * criterions as {@code p} is at least {@code r}.
     *
     * @param r The minimum average rating for elements to be included in the iterator.
     * @return An iterator over filtered elements.
     */
    @Override
    public Iterator<X> iterator(R r) {
        increment("RatedSet#iterator(R)");
        return new DelegateIterator<>(super.iterator(r));
    }

    /**
     * Returns an iterator over all criteria in the set.
     *
     * @return An iterator over all criteria in the set.
     */
    @Override
    public Iterator<P> criterions() {
        increment("RatedSet#criterions()");
        return new DelegateIterator<>(super.criterions());
    }

    /**
     * Checks if the given object is equal to this StatSet.
     *
     * @param o The object to compare with this StatSet.
     * @return true if the parameter is a StatSet and the statistics are equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (this == o) result = true;
        else if (o != null && getClass() == o.getClass()) {
            StatSet<?, ?, ?> other = (StatSet<?, ?, ?>) o;
            result = Objects.equals(statistics, other.statistics);
        }
        increment("Object#equals()");
        return result;
    }

    /**
     * Computes the hash code for this StatSet.
     *
     * @return The hash code of this StatSet based on its statistics.
     */
    @Override
    public int hashCode() {
        increment("Object#hashCode()");
        return Objects.hash(statistics);
    }

    /**
     * Performs the given action for each element of the set until all elements have been processed
     * or the action throws an exception.
     *
     * @param action The action to be performed for each element.
     */
    @Override
    public void forEach(Consumer<? super X> action) {
        increment("Iterable#forEach(Consumer)");
        super.forEach(action);
    }

    /**
     * Creates a Spliterator over the elements described by this Iterable.
     *
     * @return A Spliterator over the elements in the set.
     */
    @Override
    public Spliterator<X> spliterator() {
        increment("Iterable#spliterator()");
        return super.spliterator();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        increment("Object#clone()");
        return super.clone();
    }

    @Override
    public String toString() {
        increment("Object#toString()");
        return super.toString();
    }

    /**
     * Iterator implementation that delegates its methods to another iterator.
     *
     * @param <E> The type of elements returned by this iterator.
     */
    protected class DelegateIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;

        public DelegateIterator(Iterator<E> delegate) {
            increment("DelegateIterator#<init>(Iterator)");
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            increment("Iterator#hasNext()");
            return delegate.hasNext();
        }

        @Override
        public E next() {
            increment("Iterator#next()");
            return delegate.next();
        }

        @Override
        public void remove() {
            increment("Iterator#remove()");
            delegate.remove();
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            increment("Iterator#forEachRemaining(Consumer)");
            delegate.forEachRemaining(action);
        }
    }
}
