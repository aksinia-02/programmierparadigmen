package aufgabe6;

/**
 * Nest with humidifier to increase the humidity
 */
@Meta(author = Meta.Author.VOROBEVA)
public class NestWithHumidifier extends Nest {

    private final double waterContainerVolume;

    @Contract(pre = "height > 0, width > 0, volume > 0")
    public NestWithHumidifier(double height, double width, Material material, double volume) {
        super(height, width, material);
        this.waterContainerVolume = volume;
    }

    @Meta(author = Meta.Author.PRIVAS)
    public double waterContainerVolume() {
        return waterContainerVolume;
    }

    @Override
    public String toString() {
        return "Nest with humidifier, number: " + number() + ", volume: " + volume() + "cm3, power: " + waterContainerVolume + "cm3, material: " + material().toString() + ".";
    }
}
