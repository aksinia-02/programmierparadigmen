package aufgabe6;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Meta(author = Meta.Author.PRIVAS)
public abstract class Nest {

    private static final AtomicInteger nextNumber = new AtomicInteger(0);
    private final int frameSize;

    @Invariant("cannot be null")
    private final Measurements measurements;
    private final int number;
    @Invariant("cannot be null")
    private Material material;

    @Contract(pre = "material cannot be null")
    @Meta(author = Meta.Author.VOROBEVA)
    public Nest(double height, double width, Material material) {
        Objects.requireNonNull(material);
        this.number = nextNumber.incrementAndGet();
        this.frameSize = 2; //2cm
        this.measurements = new Measurements(width, height);
        this.material = material;
    }

    @Meta(author = Meta.Author.VOROBEVA)
    public int number() {
        return number;
    }

    public double volume() {
        return height() * width() * frameSize;
    }

    public double height() {
        return measurements.height();
    }

    public double width() {
        return measurements.width();
    }

    @History("set new material remove forever previous material")
    @Contract(pre = "must not be null")
    public void setMaterial(Material material) {
        this.material = material;
    }

    @Meta(author = Meta.Author.VOROBEVA)
    public Material material() {
        return material;
    }

    public Object materialCharacteristic() {
        return material.characteristics();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nest that = (Nest) o;
        return number == that.number;
    }

    @Meta(author = Meta.Author.VOROBEVA)
    @Override
    public abstract String toString();
}
