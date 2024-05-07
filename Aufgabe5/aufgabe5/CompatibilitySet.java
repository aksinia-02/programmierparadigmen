package aufgabe5;

import java.util.Iterator;

public class CompatibilitySet<X extends Rated<? super X, R>, R extends Calc<R>> extends StatSet<X, X, R> {

    /**
     * Returns a non-null rating calculated from the combinations of values and criterions in the CompatibilitySet.
     *
     * @return The calculated non-null rating.
     * @throws IllegalStateException if the set is empty or does not contain any criterions.
     */
    public R rated() {
        increment("CompatabilitySet#rated(X)");
        R sum = null;
        int count = 0;
        for (X value : values) {
            for (X criterion : criterions) {
                R rating = value.rated(criterion);
                sum = sum == null ? rating : sum.sum(rating);
                count++;
            }
        }
        if (sum == null) {
            throw new IllegalStateException("No rating can be determined");
        }

        return sum.ratio(count);
    }

    public Iterator<X> identical() {
        increment("CompatabilitySet#identical(X)");
        return new DelegateIterator<>(new FilteredIterator<>(values.iterator(), criterions::contains));
    }

    /**
     * Compares this CompatibilitySet with another object for equality.
     * @param o The object to compare with this CompatibilitySet.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (this == o) result = true;
        else if (o != null && getClass() == o.getClass()) {
            CompatibilitySet<?, ?> other = (CompatibilitySet<?, ?>) o;
            // Note: Cannot compare size since duplicates are allowed
            result = equalsOrderIndependent(values, other.values) && equalsOrderIndependent(criterions, other.criterions);
        }
        increment("Object#equals()");
        return result;
    }

    /**
     * Checks if two iterables are equal, regardless of the order of their elements
     * @param a The first iterable to compare.
     * @param b The second iterable to compare.
     * @return True if the iterables contain the same elements, regardless of order; false otherwise.
     */
    private static boolean equalsOrderIndependent(Iterable<?> a, Iterable<?> b) {
        for (Object o : a) {
            if (objectNotInList(b, o)) return false;
        }

        for (Object o : b) {
            if (objectNotInList(a, o)) return false;
        }

        return true;
    }

    /**
     * checks if the specified object 'o' is not present in the given list.
     * @param list The Iterable to search for the object.
     * @param o The object to check for presence in the list.
     * @return true if 'o' is not found, and false otherwise.
     */
    private static boolean objectNotInList(Iterable<?> list, Object o) {
        for (Object element : list) {
            if (element == o) {
                return false;
            }
        }
        return true;
    }
}
