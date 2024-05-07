package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

/**
 * Formicarien können änderbar (vor allem erweiterbar) sein, aber nicht jedes Formicarium ist änderbar.
*/
 public abstract class Formicarium implements FormicariumPart, Iterable<FormicariumPart> {

    /**
     * "Die Methode iterator gibt einen neuen Iterator zurück, der
     * über alle Bestandteile des Formicariums iteriert"
     * Postcondition: returns an iterator which can be used to list the parts
     * iterator is not null
     */
    public abstract Iterator<FormicariumPart> iterator();


    /**
     * Returns an object of type Compatability that describes the environmental conditions provided by the Formicarium.
     * Precondition: The Formicarium must not be null.
     * Postcondition: The returned Compatability object(not null) represents the combined environmental conditions of the Formicarium
     *               and all its constituent FormicariumPart items. The selfCompatability method is used to get the initial
     *               compatibility, and then it is updated by iterating through each FormicariumPart and combining their
     *               compatibilities using the compatible method.
     *
     * @return A Compatability object describing the environmental conditions of the Formicarium.
     */
    @Override
    public Compatability compatability() {
        Compatability compatibility = selfCompatability();
        for (FormicariumPart item : this) {
            if (item == this) continue;
            compatibility = compatibility.compatible(item.compatability());
        }
        return compatibility;
    }

    /**
     * Postcondition: Compatability object is not null
     */
    protected abstract Compatability selfCompatability();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Formicarium that = (Formicarium) o;
        Iterator<FormicariumPart> thisIter = iterator();
        Iterator<FormicariumPart> thatIter = that.iterator();
        while (thisIter.hasNext() && thatIter.hasNext()) {
            if(!thisIter.next().equals(thatIter.next())) return false;
        }
        if(thisIter.hasNext() != thatIter.hasNext()) return false;
        return Objects.equals(compatability(), that.compatability());
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(compatability());
        for(FormicariumPart part : this) {
            hash = Objects.hash(hash, part);
        }
        return hash;
    }

}
