package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * "Eine (möglicherweise leere) Menge von Formicarien oder Bestandteilen
 * von Formicarien oder Messgeräten bzw. Werkzeugen, die zusammen mit Formicarien verwendbar sind, unabhängig davon,
 * ob sie als Teil eines Formicariums angesehen werden oder nicht.
 */
public class FormicariumSet implements Iterable<FormicariumItem> {
    private final List<UniqueItem> items = new ArrayList<>();


    /**
     * "Eine Methode add mit einem Parameter in einem Objekt von FormicariumSet fügt ein neues Element hinzu,
     * sofern das identische Element nicht schon vorhanden ist (gleiche Elemente können jedoch mehrfach vorhanden sein)"
     * Postconditions: Adds the specified item to this FormicariumSet if it is not already contained.
     */
    public void add(FormicariumItem item) {
        if (item == null) return;
        UniqueItem uniqueItem = new UniqueItem(item);
        if (items.contains(uniqueItem)) return;
        items.add(uniqueItem);
    }

    /**
     * "Über einen Iterator sind die jeweils voneinander verschiedenen
     * Elemente der Menge auflistbar (gleiche Elemente nicht mehrfach)."
     * Postconditions: SetIterator is not null
     */
    public SetIterator iterator() {
        return new SetIterator();
    }


    /**
     * A record representing a unique item with a non-null FormicariumItem.
     * Precondition: item is not null
     */
    private record UniqueItem(FormicariumItem item) {

        /**
         * Postconditions: If the compared objects are the same instance, they are equal.
         * If the compared object is null or not of the same class, they are not equal.
         * Two UniqueItem instances are considered equal if their contained FormicariumItems are the same.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UniqueItem setItem = (UniqueItem) o;
            return item == setItem.item;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(item);
        }

    }

    /**
     * An iterator for a set of FormicariumItem instances.
     */
    public class SetIterator implements Iterator<FormicariumItem> {
        // The representatives represent a group of 'equal' items
        private final Iterator<FormicariumItem> representatives;
        private boolean canRemove;
        private FormicariumItem lastReturned;

        private SetIterator() {
            this.representatives = items.stream().map(i -> i.item).distinct().iterator();
            this.canRemove = false;
            lastReturned = null;
        }

        /**
         * Returns the count of elements equal to the last returned item.
         *
         * Postcondition: returns the count of elements equal to the last returned item.
         */
        public int count() {
            if (lastReturned == null) return 0;
            return (int) items.stream().filter(i -> Objects.equals(i.item, lastReturned)).count();
        }

        /**
         * "Die Methode remove des Iterators (ohne Argument)
         * verringert die Anzahl vorhandener Elemente (gleich dem, das zuletzt von next zurückgegeben wurde) um 1"
         * Precondition: Can only be called once after next()
         */
        @Override
        public void remove() {
            if (!canRemove) throw new NoSuchElementException();
            canRemove = false;
            items.remove(new UniqueItem(lastReturned));
        }


        /**
         * eine Methode remove des Iterators mit einer Zahl gößer 0 als Argument um die gegebene Anzahl gleicher Elemente,
         * sofern eine ausreichende Zahl gleicher Elemente vorhanden ist.
         *
         * If count is larger than the number of elements n, only n elements are removed.
         * If count is <= 0 no elements are removed.
         * Precondition: Can only be called once after next()
         * Postcondition: Removes a specified number of elements equal to the last returned item from the underlying collection.
         *
         * @param count The number of elements to remove.
         * @throws NoSuchElementException If remove is called without a preceding call to next.
         */
        public void remove(int count) {
            if (!canRemove) throw new NoSuchElementException();
            canRemove = false;

            AtomicInteger countAtomic = new AtomicInteger(count);
            items.removeIf(i -> {
                if (countAtomic.get() <= 0) return false;
                if (!Objects.equals(i.item, lastReturned)) return false;
                countAtomic.decrementAndGet();
                return true;
            });
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return The next element in the iteration.
         * @throws NoSuchElementException If there are no more elements.
         */
        @Override
        public FormicariumItem next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            canRemove = true;
            lastReturned = representatives.next();
            return lastReturned;
        }

        /**
         * Returns true if there are more elements in the iteration.
         *
         * @return True if there are more elements, false otherwise.
         */
        @Override
        public boolean hasNext() {
            return representatives.hasNext();
        }
    }
}
