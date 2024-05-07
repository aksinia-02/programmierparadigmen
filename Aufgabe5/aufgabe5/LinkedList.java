package aufgabe5;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * A simple linked list implementation.
 *
 * @param <E> The type of elements in the list.
 */
public final class LinkedList<E> implements Iterable<E> {

    private Node<E> head;
    private Node<E> tail;
    private int size;

    /**
     * Returns the value at the given index.
     * Throws an {@code ArrayIndexOutOfBoundsException} for out-of-bounds indices.
     *
     * @param i The index to retrieve.
     * @return The value at the given index.
     */
    public E get(int i) {
        return getNode(i).value;
    }

    private Node<E> getNode(int i) {
        if (i < 0 || i >= size) throw new ArrayIndexOutOfBoundsException(i);
        Node<E> n = head;
        while (--i >= 0) n = n.next;
        return n;
    }

    /**
     * Sets the value at the given index.
     * Throws an {@code ArrayIndexOutOfBoundsException} for out-of-bounds indices.
     *
     * @param i       The index where the value is to be set.
     * @param element The value to be set at the specified index.
     */
    public void set(int i, E element) {
        getNode(i).value = element;
    }

    /**
     * Appends the value to the end of the list.
     *
     * @param element The value to be appended.
     */
    public void add(E element) {
        Node<E> n = new Node<>(element);
        if (size == 0) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    /**
     * Returns true if the element is contained in this list, using {@link Objects#equals(Object, Object)}.
     *
     * @param element The element to search for.
     * @return True if the element is contained, false otherwise.
     */
    public boolean contains(E element) {
        return indexOf(element) != -1;
    }

    /**
     * Returns the index of the element using {@link Objects#equals(Object, Object)}.
     *
     * @param element The element to find the index of.
     * @return The index of the element, or -1 if not found.
     */
    public int indexOf(E element) {
        return indexOf(element, Objects::equals);
    }

    /**
     * Returns the index of the element using the provided predicate.
     *
     * @param element   The element to find the index of.
     * @param predicate The predicate for comparison.
     * @return The index of the element, or -1 if not found.
     */
    public int indexOf(E element, BiPredicate<E, E> predicate) {
        int i = 0;
        Node<E> n = head;
        while (n != null) {
            if (predicate.test(element, n.value)) {
                return i;
            }
            n = n.next;
            i++;
        }
        return -1;
    }

    /**
     * Returns true if the element is contained in this list, using the provided predicate.
     *
     * @param element   The element to search for.
     * @param predicate The predicate for comparison.
     * @return True if the element is contained, false otherwise.
     */
    public boolean contains(E element, BiPredicate<E, E> predicate) {
        return indexOf(element, predicate) != -1;
    }

    /**
     * Removes the element from the list, using Objects.equals
     *
     * @param element   The element to remove.
     */
    public void remove(E element) {
        int index = indexOf(element);
        if (index == -1) return;
        remove(index);
    }

    /**
     * Removes the entry at the given index.
     *
     * @param i The index to remove.
     */
    public void remove(int i) {
        Node<E> prev = getPrevNode(i);
        size--;
        if (prev == null) {
            Node<E> curr = head;
            head = head.next;
            curr.next = null;
            return;
        }
        assert prev.next != null;
        Node<E> curr = prev.next;
        if (curr.next == null) {
            prev.next = null;
            tail = prev;
        } else {
            prev.next = curr.next;
            curr.next = null;
        }
    }

    /**
     * Returns the node preceding the node at the given index.
     * Throws an {@code ArrayIndexOutOfBoundsException} for out-of-bounds indices.
     * Returns {@code null} if the index is 0.
     *
     * @param i The index for which to find the preceding node.
     * @return The node preceding the node at the given index, or {@code null} if the index is 0.
     */
    private Node<E> getPrevNode(int i) {
        if (i < 0 || i >= size) throw new ArrayIndexOutOfBoundsException(i);
        if (i == 0) return null;
        Node<E> n = head;
        while (--i > 0) n = n.next;
        return n;
    }

    /**
     * Removes and returns the first element from the list.
     *
     * @return The removed element, or {@code null} if the list is empty.
     */
    public E popFirst() {
        if (head == null) return null;
        size--;
        Node<E> curr = head;
        head = head.next;
        curr.next = null;
        if (size == 0) tail = null;
        return curr.value;
    }

    /**
     * Removes the element from the list, using the predicate
     * @param element   The element to be removed.
     * @param predicate The predicate for comparison.
     */
    public void remove(E element, BiPredicate<E, E> predicate) {
        int index = indexOf(element, predicate);
        if (index == -1) return;
        remove(index);
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return The number of elements in this list.
     */
    public int size() {
        return size;
    }

    public static class Node<E> {
        public Node<E> next;
        public E value;

        public Node(E value) {
            this.value = value;
        }
    }

    /**
     * An iterator for traversing the linked list.
     */
    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new LinkedIterator(head);
    }

    private class LinkedIterator implements Iterator<E> {
        private Node<E> next;
        private Node<E> curr;
        private Node<E> prev;

        public LinkedIterator(Node<E> head) {
            next = head;
        }

        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            E value = next.value;
            prev = curr;
            curr = next;
            next = next.next;
            return value;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public void remove() {
            if (curr == null) throw new NoSuchElementException();
            size--;
            if (curr == head) {
                head = head.next;
            }
            if (curr == tail) {
                tail = prev;
                if (prev != null) prev.next = null;
            }
            curr.next = null;
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
