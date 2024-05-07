package aufgabe4;


// Ein Formicarium oder ein Bestandteil eines Formicariums oder ein Messgerät bzw. Werkzeug, das zusammen mit
// Formicarien verwendbar ist, unabhängig davon, ob es als Teil eines Formicariums angesehen werden kann oder nicht.
public interface FormicariumItem {
    // "Die Methode compatibility gibt ein Objekt vom Typ Compatibility zurück"

    /**
     * Postconditions: Returns the compatability of this item or
     * throw an IncompatibleException if no compatability can be determined.
     * Compatability object is not null
     */
    Compatability compatability() throws IncompatibleException;
}
