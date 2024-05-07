package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// Forceps ist kein Formicarium oder Bestandteil eines Formicariums."
public class Forceps implements Instrument, FormicariumItem {

    private final Compatability compatability;
    private final InstrumentQuality quality;

    /**
     * Preconsition: quality is not null
     * @param minSize minimale Größe der Ameisen in mm
     * @param maxSize maximale Größe der Ameisen in mm
     * @param quality quality of Instrument
     */
    public Forceps(float minSize, float maxSize, InstrumentQuality quality) {
        compatability = new Compatability();
        compatability.setMinSize(minSize);
        compatability.setMaxSize(maxSize);
        this.quality = quality;
    }

    /**
     * "Die Methode compatibility gibt ein Objekt vom Typ Compatibility zurück,
     * das (neben irrelevanten anderen Bedingungen) den Größenbereich von Ameisen angibt,
     * für den die Pinzette gut einsetzbar ist."
     * Postcondition: Compatability object is not null
     */
    @Override
    public Compatability compatability() {
        return compatability;
    }

    /**
     * Postcondition: InstrumentQuality is not null
     */
    @Override
    public InstrumentQuality quality() {
        return quality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Forceps forceps = (Forceps) o;
        return Objects.equals(compatability, forceps.compatability) && quality == forceps.quality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(compatability, quality);
    }
}
