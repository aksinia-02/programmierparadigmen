package aufgabe6;

@Meta(author = Meta.Author.VOROBEVA)
public class SandClay implements Material {
    private final double weight;

    @Contract(pre = "weight > 0")
    public SandClay(double weight) {
        this.weight = weight;
    }

    @Meta(author = Meta.Author.PRIVAS)
    public double weight() {
        return weight;
    }

    @Meta(author = Meta.Author.PRIVAS)
    @Override
    public Double characteristics() {
        return weight;
    }

    @Override
    public String toString() {
        return "sand clay with weight: " + weight + "kg";
    }
}
