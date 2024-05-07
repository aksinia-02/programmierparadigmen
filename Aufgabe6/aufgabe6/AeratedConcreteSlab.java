package aufgabe6;

@Meta(author = Meta.Author.VOROBEVA)
public class AeratedConcreteSlab implements Material {

    @Invariant("thickness is always equals 2")
    private final int thickness;

    @Invariant("not null")
    private final Measurements measurements;

    @Contract(
        pre = "height > 0 and width > 0",
        post = "set height, width and thickness = 2 for aerated Concrete Slab")
    public AeratedConcreteSlab(double height, double width) {
        this.thickness = 2;
        measurements = new Measurements(width, height);
    }

    @Contract(
        post = "return volume"
    )
    @Meta(author = Meta.Author.PRIVAS)
    public double volume() {
        return measurements.width() * measurements.height() * thickness;
    }

    @Meta(author = Meta.Author.PRIVAS)
    @Override
    public Object characteristics() {
        return measurements;
    }

    @Meta(author = Meta.Author.VOROBEVA)
    @Override
    public String toString() {
        return "aerated concrete slab with thick: " + thickness + "cm, height: " + measurements.height + "cm, width: " + measurements.width + "cm";
    }
}
