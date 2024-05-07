package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * "Ein Gerät zur Messung der Temperatur ist ein unverzichtbarer Bestandteil eines Formicariums für Ameisen,
 * die bestimmte Temperaturniveaus benötigen.
 * Als Objekte von Thermometer werden hier nur solche Temperaturmessgeräte angesehen,
 * die Bestandteil eines Formicariums sein können."
 */
public class Thermometer implements Instrument, FormicariumPart {
    private final Compatability compatability;
    private final InstrumentQuality quality;

    /**
     * Precondition: quality is not null
     * @param minTemperature minimale Temperatur in ◦C
     * @param maxTemperature maximale Temperatur in ◦C
     * @param quality quality of Instrument
     */
    public Thermometer(float minTemperature, float maxTemperature, InstrumentQuality quality) {
        compatability = new Compatability();
        compatability.setMinTemperature(minTemperature);
        compatability.setMaxTemperature(maxTemperature);
        this.quality = quality;
    }

    /**
     *
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
        Thermometer that = (Thermometer) o;
        return Objects.equals(compatability, that.compatability) && quality == that.quality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(compatability, quality);
    }
}
