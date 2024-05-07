package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Ein Objekt von Compatibility ist nie gleich einem Objekt, das ein physisches Objekt der realen Welt darstellt."
 */
public class Compatability {
    /**
     * "Die Beschreibung erfolgt über Wertebereiche verschiedener Umweltparameter"
     * "Wenn ein von Compatibility beschriebenes Umweltkriterium nicht
     * relevant ist, wird dafür der größtmögliche Wertebereich angenommen."
     */
    private float minSize = Float.NEGATIVE_INFINITY;
    private float maxSize = Float.POSITIVE_INFINITY;
    private float minTemperature = Float.NEGATIVE_INFINITY;
    private float maxTemperature = Float.POSITIVE_INFINITY;
    private float minHumidity = Float.NEGATIVE_INFINITY;
    private float maxHumidity = Float.POSITIVE_INFINITY;
    /**
     * Dauer der Ameisenhaltung mit diesem Gegenstand zurück – eine Stunde, einen
     * Tag, eine Woche, ein Monat, ein Jahr oder unbeschränkt
     */
    private Time time = Time.INFINITE;
    private Time maxTime = Time.INFINITE;

    /**
     * @return minSize geeignete minimale Größe der Ameisen in mm
     */
    public float minSize() {
        return minSize;
    }

    /**
     * @param minSize geeignete minimale Größe der Ameisen in mm
     */
    public void setMinSize(float minSize) {
        this.minSize = minSize;
    }

    /**
     * @return minSize geeignete maximale Größe der Ameisen in mm
     */
    public float maxSize() {
        return maxSize;
    }

    /**
     * @param maxSize geeignete maximale Größe der Ameisen in mm
     */
    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * @return minimale Temperatur in ◦C
     */
    public float minTemperature() {
        return minTemperature;
    }

    /**
     * @param minTemperature minimale Temperatur in ◦C
     */
    public void setMinTemperature(float minTemperature) {
        this.minTemperature = minTemperature;
    }

    /**
     * @return maximale Temperatur in ◦C
     */
    public float maxTemperature() {
        return maxTemperature;
    }

    /**
     * @param maxTemperature maximale Temperatur in ◦C
     */
    public void setMaxTemperature(float maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    /**
     * @return minimale relative Luftfeuchtigkeit in %
     */
    public float minHumidity() {
        return minHumidity;
    }

    /**
     * @param minHumidity minimale relative Luftfeuchtigkeit in %
     */
    public void setMinHumidity(float minHumidity) {
        this.minHumidity = minHumidity;
    }

    /**
     * @return maximale relative Luftfeuchtigkeit in %
     */
    public float maxHumidity() {
        return maxHumidity;
    }

    /**
     * @param maxHumidity maximale relative Luftfeuchtigkeit in %
     */
    public void setMaxHumidity(float maxHumidity) {
        this.maxHumidity = maxHumidity;
    }

    /**
     * "Zusätzlich geben die Methoden time und maxTime je einen zeitlichen Horizont für
     * die längstmögliche Dauer der Ameisenhaltung mit diesem Gegenstand zurück"
     */
    public Time time() {
        return time;
    }

    /**
     * Durch setTime kann der Wert von time angepasst werden, wobei jedoch kein Wert gesetzt werden kann,
     * der über maxTime hinausgeht.
     * Postcondition: time >= maxTime
     */
    public void setTime(Time time) {
        if (time.compareTime(maxTime) > 0) throw new IllegalArgumentException("time must be less than maxTime");
        this.time = time;
    }

    public Time maxTime() {
        return maxTime;
    }

    /**
     * Postcondition: Wert kann nie kleiner als time sein.
     */
    public void setMaxTime(Time maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * "Die Methode compatible mit einem Argument von Compatibility gibt ein Objekt
     * von Compatibility zurück"
     * Postcondition: Compatability object is not null
     */
    public Compatability compatible(Compatability other) throws IncompatibleException {

        if (other == null) {
            throw new IncompatibleException("Incompatible as other compatability is null");
        }

        Compatability result = new Compatability();

        // "das für time und maxTime die jeweils kleineren Werte aus this und dem Argument übernimmt"
        result.time = (this.time.compareTime(other.time) < 0) ? this.time : other.time;
        result.maxTime = (this.maxTime.compareTime(other.maxTime) < 0) ? this.maxTime : other.maxTime;

        // "und für Umweltparameter die überschneidenden Wertebereiche annimmt"
        result.minSize = Math.max(this.minSize, other.minSize);
        result.maxSize = Math.min(this.maxSize, other.maxSize);
        result.minHumidity = Math.max(this.minHumidity, other.minHumidity);
        result.maxHumidity = Math.min(this.maxHumidity, other.maxHumidity);
        result.minTemperature = Math.max(this.minTemperature, other.minTemperature);
        result.maxTemperature = Math.min(this.maxTemperature, other.maxTemperature);

        // "überschneiden sich Wertebereiche für einen Umweltparameter nicht, wird eine Ausnahme ausgelöst"
        if (result.minSize > result.maxSize) {
            throw new IncompatibleException("size", this, other, Compatability::minSize, Compatability::maxSize);
        }

        if (result.minHumidity > result.maxHumidity) {
            throw new IncompatibleException("humidity", this, other, Compatability::minHumidity, Compatability::maxHumidity);
        }

        if (result.minTemperature > result.maxTemperature) {
            throw new IncompatibleException("temperature", this, other, Compatability::minTemperature, Compatability::maxTemperature);
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compatability that = (Compatability) o;
        return Float.compare(minSize, that.minSize) == 0 && Float.compare(maxSize, that.maxSize) == 0 && Float.compare(minTemperature, that.minTemperature) == 0 && Float.compare(maxTemperature, that.maxTemperature) == 0 && Float.compare(minHumidity, that.minHumidity) == 0 && Float.compare(maxHumidity, that.maxHumidity) == 0 && this.time.compareTime(that.time) == 0 && maxTime.compareTime(that.maxTime) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minSize, maxSize, minTemperature, maxTemperature, minHumidity, maxHumidity, time, maxTime);
    }
}
