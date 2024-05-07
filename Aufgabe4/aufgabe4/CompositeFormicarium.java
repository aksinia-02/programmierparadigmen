package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.*;

// Ein Formicarium, das durch Hinzufügen und Wegnehmen von Bestandteilen änderbar ist.
public class CompositeFormicarium extends ExpandableFormicarium {
    private final List<FormicariumPart> parts = new ArrayList<>();

    // "Über einen Iterator sind alle Bestandteile zugreifbar"

    /**
     * Returns an iterator which can be used to list and remove parts.
     * Removing a part will not change this formicarium's identity but it's equality.
     * Postcondition: Iterator object is not null
     */
    @Override
    public Iterator<FormicariumPart> iterator() {
        return new CompositeFormicariumIterator();
    }

    /**
     * The composite formicarium is not a physical object and as such
     * does not have any compatability restrictions by itself
     * Postcondition: compatability object is not null
     */
    @Override
    protected Compatability selfCompatability() {
        return new Compatability();
    }

    // "Die Methode add in einem Objekt von CompositeFormicarium fügt einen Bestandteil hinzu"

    /**
     * Adds the specified part to this formicarium if it is not already contained.
     * Adding a part will not change this formicarium's identity but it's equality.
     * Precondition: part is not null
     */
    public void add(FormicariumPart part) {
        // "sofern er [...] nicht schon mit derselben Identität vorkommt (nicht-idente
        // gleiche Bestandteile können mehrfach vorkommen)."
        if (parts.contains(part)) return;
        if (part == this) return;
        try {
            // "Kompatibilität ist gegeben, wenn compatible aus Compatibility angewandt auf
            // die Umweltbeschreibungen des Formicariums und neuen Bestandteils keine Ausnahme auslöst."
            compatability().compatible(part.compatability());
            parts.add(part);
        } catch (IncompatibleException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected class CompositeFormicariumIterator implements Iterator<FormicariumPart> {
        private int index = 0;
        private boolean remove = false;

        @Override
        public boolean hasNext() {
            return index < parts.size();
        }

        @Override
        public FormicariumPart next() {
            remove = true;
            if (hasNext()) {
                return parts.get(index++);
            }
            throw new NoSuchElementException();
        }


        /**
         * "remove im Iterator entfernt den zuletzt von next zurückgegebenen Bestandteil wenn mehr als ein
         * Bestandteil vorhanden ist, sonst wird eine Ausnahme ausgelöst."
         * Precondition: Can only be called once after next()
         */
        @Override
        public void remove() {
            if (!remove) throw new NoSuchElementException();
            remove = false;
            parts.remove(--index);
        }
    }
}
