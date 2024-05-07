package aufgabe5;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * An abstract base class implementing the {@link RatedSet} interface.
 *
 * @param <X> The type of elements in the set.
 * @param <P> The type of criteria for rating elements.
 * @param <R> The type of rating results.
 */
public abstract class AbstractRatedSet<X extends Rated<? super P, R>, P, R extends Calc<R>> implements RatedSet<X, P, R> {

    // Storage for rated elements and criteria.
    protected final LinkedList<X> values = new LinkedList<>();
    protected final LinkedList<P> criterions = new LinkedList<>();

    /**
     * Adds an element to the set, unless it is already contained.
     *
     * @param x The element to add. Must not be null.
     */
    @Override
    public void add(X x) {
        Objects.requireNonNull(x);
        if (values.contains(x, AbstractRatedSet::identityEquals)) return;
        values.add(x);
    }

    protected static boolean identityEquals(Object a, Object b) {
        return a == b;
    }

    /**
     * Adds a criterion to the set, regardless of whether it is already contained.
     *
     * @param p The criterion to add. Must not be null.
     */
    @Override
    public void addCriterion(P p) {
        Objects.requireNonNull(p);
        criterions.add(p);
    }

    /**
     * Returns an iterator over all elements of type X.
     * The Iterator can be used to remove elements.
     *
     * @return An iterator over the elements.
     */
    @Override
    @NotNull
    public Iterator<X> iterator() {
        return values.iterator();
    }

    /**
     * Returns an iterator over all elements of type X,
     * where {@code x.rated(p).atLeast(r)} is true.
     * The Iterator can be used to remove elements.
     *
     * @param p The criterion for filtering elements.
     * @param r The minimum rating for filtering elements.
     * @return An iterator over the filtered elements.
     */
    @Override
    public Iterator<X> iterator(P p, R r) {
        return new FilteredIterator<>(values.iterator(), x -> x.rated(p).atLeast(r));
    }

    /**
     * Returns an iterator over all elements of type X,
     * where the average of {@code x.rated(p)} with all criterions as {@code p} is at least {@code r}.
     * May throw an IllegalStateException if no average can be determined.
     * The Iterator can be used to remove elements.
     *
     * @param r The minimum average rating for filtering elements.
     * @return An iterator over the filtered elements.
     */
    @Override
    public Iterator<X> iterator(R r) {
        return new FilteredIterator<>(values.iterator(), x -> {
            R sum = null;
            for (P criterion : criterions) {
                R rating = x.rated(criterion);
                if (sum == null) sum = rating;
                else sum = sum.sum(rating);
            }
            if (sum == null) throw new IllegalStateException("No average can be determined");
            return sum.ratio(criterions.size()).atLeast(r);
        });
    }

    /**
     * Returns an iterator over all criterions.
     * The Iterator can be used to remove elements.
     *
     * @return An iterator over the criterions.
     */
    @Override
    public Iterator<P> criterions() {
        return criterions.iterator();
    }

    /**
     * A generic iterator that filters elements based on a provided predicate.
     *
     * @param <E> The type of elements in the iterator.
     */
    protected static class FilteredIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private final Predicate<E> filter;
        private E next;

        public FilteredIterator(Iterator<E> delegate) {
            this.delegate = delegate;
            this.filter = (e) -> true;
            this.next = findNext();
        }

        public FilteredIterator(Iterator<E> delegate, Predicate<E> filter) {
            this.delegate = delegate;
            this.filter = filter;
            this.next = findNext();
        }

        protected E findNext() {
            while (delegate.hasNext()) {
                E next = delegate.next();
                if (filter.test(next)) {
                    return next;
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public E next() {
            E current = this.next;
            this.next = findNext();
            return current;
        }

        @Override
        public void remove() {
            if (this.next == null) throw new NoSuchElementException();
            delegate.remove();
        }
    }
}
