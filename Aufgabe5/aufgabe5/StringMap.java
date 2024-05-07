package aufgabe5;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class StringMap<V> {

    private final LinkedList<String> keys = new LinkedList<>();
    // Starting the tree with 0 should make the tree more balanced, I think
    private final Node<V> root = new Node<>(null, 0, null);

    /**
     * Returns the associated entry or the default value.
     * Key may be null.
     */
    public V getOrDefault(String key, V def) {
        Node<V> n = getNode(key);
        if (n == null) return def;
        return n.value;
    }

    private Node<V> getNode(String key) {
        if (key == null) return null;
        int hash = hash(key);

        Node<V> n = root;
        while (n != null) {
            if (hash == n.hash && key.equals(n.key)) {
                return n;
            }
            if (hash > n.hash) {
                n = n.gt;
            } else {
                n = n.le;
            }
        }
        return null;
    }

    private static int hash(String key) {
        // hashCode alone is not very uniform, this adds a single iteration of the Jenkins Hash Function
        int hash = key.hashCode();
        hash += hash << 10;
        hash ^= hash >> 6;
        hash += hash << 3;
        hash ^= hash >> 11;
        hash += hash << 15;
        return hash;
    }

    /**
     * Returns true if the key is contained in this map.
     * Key may be null
     */
    public boolean contains(String key) {
        return getNode(key) != null;
    }

    /**
     * Sets the associated value for the key.
     * Key must not be null.
     */
    public void put(String key, V value) {
        Node<V> n = getNode(key);
        if (n != null) {
            n.value = value;
            return;
        }

        insert(key, value);
    }

    private void insert(String key, V value) {
        if (key == null) return;
        keys.add(key);

        int hash = hash(key);
        Node<V> curr = root;
        while (true) {
            if (hash > curr.hash) {
                if (curr.gt == null) break;
                curr = curr.gt;
            } else {
                if (curr.le == null) break;
                curr = curr.le;
            }
        }

        Node<V> n = new Node<>(key, hash, value);
        if (hash > curr.hash) {
            curr.gt = n;
        } else {
            curr.le = n;
        }
    }

    public Iterable<String> keys() {
        return keys;
    }

    public Iterable<Map.Entry<String, V>> entries() {
        return () -> new TreeIterator<>(root);
    }

    private static class TreeIterator<V> implements Iterator<Map.Entry<String, V>> {
        private final LinkedList<Node<V>> queue = new LinkedList<>();

        public TreeIterator(Node<V> root) {
            if (root.le != null) queue.add(root.le);
            if (root.gt != null) queue.add(root.gt);
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public Map.Entry<String, V> next() {
            if (queue.isEmpty()) throw new NoSuchElementException();
            Node<V> n = queue.popFirst();
            if (n.le != null) queue.add(n.le);
            if (n.gt != null) queue.add(n.gt);
            return Map.entry(n.key, n.value);
        }
    }

    /**
     * Checks for equality by comparing all keys and values.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringMap<?> other = (StringMap<?>) o;
        if (other.size() != size()) return false;
        for (String key : keys) {
            Object a = get(key);
            Object b = other.get(key);
            if (!Objects.equals(a, b)) return false;
        }
        return true;
    }

    /**
     * Returns the count of entries.
     */
    public int size() {
        return keys.size();
    }

    /**
     * Returns the associated entry or null.
     * Key may be null.
     */
    public V get(String key) {
        Node<V> n = getNode(key);
        if (n == null) return null;
        return n.value;
    }

    private static class Node<V> {
        // less or equal
        public Node<V> le;
        // greater than
        public Node<V> gt;
        public final String key;
        public final int hash;
        public V value;

        public Node(String key, int hash, V value) {
            this.key = key;
            this.hash = hash;
            this.value = value;
        }
    }
}
