package aufgabe6;


import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;

@Meta(author = Meta.Author.MUTH)
public class LinkedList implements Iterable<Object> {

    private Node head;
    private Node tail;
    @Invariant("size >= 0")
    private int size;

    @Contract(
        pre = "index must be within bounds",
        post = "return the node at given index"
    )
    private Node getNode(int i) {
        if (i < 0 || i >= size) throw new ArrayIndexOutOfBoundsException(i);
        Node n = head;
        while (--i >= 0) n = n.next;
        return n;
    }

    @Contract(
        post = "the element will be added to the end of the list"
    )
    public void add(Object element) {
        Node n = new Node(element);
        if (size == 0) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    @Contract(
        post = "returns true if the element is contained in this list using object equality, otherwise false"
    )
    public boolean contains(Object element) {
        return indexOf(element) != -1;
    }

    @Contract(
        post = "returns the first index of the element in this list using object equality, otherwise -1"
    )
    public int indexOf(Object element) {
        return indexOf(element, Object::equals);
    }

    @Contract(
        post = "returns the first index of the element in this list using the provided predicate, otherwise -1"
    )
    public int indexOf(Object element, BiPredicate predicate) {
        int i = 0;
        Node n = head;
        while (n != null) {
            if (predicate.test(element, n.value)) {
                return i;
            }
            n = n.next;
            i++;
        }
        return -1;
    }

    @Contract(
        post = "removes the first occurrence of the element in this list using the provided predicate"
    )
    public void remove(Object element, BiPredicate predicate) {
        int index = indexOf(element, predicate);
        if (index == -1) return;
        remove(index);
    }

    @Contract(
        pre = "index must be within bounds",
        post = "removes the element at the specified index"
    )
    public void remove(int i) {
        Node prev = getPrevNode(i);
        size--;
        if (prev == null) {
            Node curr = head;
            head = head.next;
            curr.next = null;
            return;
        }
        assert prev.next != null;
        Node curr = prev.next;
        if (curr.next == null) {
            prev.next = null;
            tail = prev;
        } else {
            prev.next = curr.next;
            curr.next = null;
        }
    }

    @Contract(
        pre = "index must be within bounds",
        post = "returns prev node of node at given index"
    )
    private Node getPrevNode(int i) {
        if (i < 0 || i >= size) throw new ArrayIndexOutOfBoundsException(i);
        if (i == 0) return null;
        Node n = head;
        while (--i > 0) n = n.next;
        return n;
    }

    @Contract(
        post = "returns the number of elements in this list"
    )
    @Invariant("value is >= 0")
    public int size() {
        return size;
    }

    @Meta(author = Meta.Author.MUTH)
    private static class Node {
        public Node next;
        public Object value;

        public Node(Object value) {
            this.value = value;
        }
    }

    /**
     * An iterator for traversing the linked list.
     */
    @Contract(
        post = "returned value is not null"
    )
    @Override
    public Iterator<Object> iterator() {
        return new LinkedIterator(head);
    }

    @Meta(author = Meta.Author.MUTH)
    private class LinkedIterator implements Iterator<Object> {
        private Node next;
        private Node curr;
        private Node prev;

        public LinkedIterator(Node head) {
            next = head;
        }

        @Contract(
            post = "returns next value if it exists"
        )
        @Override
        public Object next() {
            if (!hasNext()) throw new NoSuchElementException();
            Object value = next.value;
            prev = curr;
            curr = next;
            next = next.next;
            return value;
        }

        @Contract(
            post = "returns true if next node exists, otherwise false"
        )
        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Contract(
            pre = "method next is called at least 1 time",
            post = "deletes the node that was last received using the next method"
        )
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

    @Contract(
        post = "returns true is this list is empty, otherwise false"
    )
    public boolean isEmpty() {
        return size == 0;
    }
}
