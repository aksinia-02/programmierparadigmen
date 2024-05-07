package aufgabe6;

@Meta(author = Meta.Author.PRIVAS)
public class Measurements {
    public double width;
    public double height;

    @Contract(pre = "width > 0, height > 0")
    public Measurements(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double height() {
        return height;
    }

    public double width() {
        return width;
    }
}
