package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public abstract class ExpandableFormicarium extends Formicarium {
    /**
     * Allows items to be added to the formicarium
     * Precondition: part is not null
     */
    public abstract void add(FormicariumPart part);

    /**
     * Returns an iterator which can be used to list and remove parts.
     * Removing a part that doesn't limit compatability will not change this formicarium's identity.
     * Postcondition: Iterator object is not null
     */
    @Override
    public abstract Iterator<FormicariumPart> iterator();
}
