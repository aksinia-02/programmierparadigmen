package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * "Ein Bestandteil eines Formicariums, das dafür vorgesehen ist eine Ameisenkönigin zu beherbergen.
 * Für kurze Zeit (wenige Tage, etwa für den Transport) kann das Nest alleine als Formicarium genutzt werden."
 */
public class Nest extends Formicarium {

    private final Compatability compatability;

    /**
     * Precondition: compatability is not null
     */

    public Nest(Compatability compatability) {
        this.compatability = compatability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nest that = (Nest) o;
        return Objects.equals(compatability, that.compatability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compatability);
    }

    /**
     * Returns an iterator over the FormicariumPart items.
     * Postcondition: The returned iterator(not null) starts with the Formicarium itself (Nest.this) and indicates that there is only
     *                one element. Subsequent calls to hasNext will return false, ensuring that the iterator represents a
     *                single-element sequence containing the Formicarium itself.
     *
     * @return An iterator over the FormicariumPart items.
     */
    public Iterator<FormicariumPart> iterator() {
        return new Iterator<>() {
            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public FormicariumPart next() {
                if (!hasNext) throw new NoSuchElementException();
                hasNext = false;
                return Nest.this;
            }
        };
    }

    /**
     * Postcondition: Compatability object is not null
     */
    @Override
    protected Compatability selfCompatability() {
        return compatability;
    }
}
