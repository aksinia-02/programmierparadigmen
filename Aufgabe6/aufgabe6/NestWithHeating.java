package aufgabe6;

/**
 * Nest with heating for increase in temperature
 **/
@Meta(author = Meta.Author.VOROBEVA)
public class NestWithHeating extends Nest {

    private final double power;

    @Contract(pre = "height > 0, width > 0, power > 0")
    public NestWithHeating(float height, float width, Material material, double power) {
        super(height, width, material);
        this.power = power;
    }

    public double power() {
        return power;
    }

    @Override
    public String toString() {
        return "Nest with heating, number: " + number() + ", volume: " + volume() + "cm3, power: " + power + "W, material: " + material().toString() + ".";
    }
}
