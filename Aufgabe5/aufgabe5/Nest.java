package aufgabe5;

import org.jetbrains.annotations.NotNull;

/**
 * A class representing a nest, extending the {@link AbstractPart} class.
 *
 * @see AbstractPart
 */
public class Nest extends AbstractPart {
    private final float antSize;

    public Nest(Quality quality, float antSize) {
        super(quality);
        this.antSize = antSize;
    }

    /**
     * Parameter part is not null
     * Returns the rating of this by the criteria parameter.
     * Returns Quality.UNUSABLE if the part is a Nest
     */
    @Override
    public Quality rated(@NotNull Part part) {
        if (part instanceof Nest) return Quality.UNUSABLE;
        return quality.sum(part.rated());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;
        return quality.equals(((Nest) o).quality) && antSize == ((Nest) o).antSize();
    }

    public float antSize() {
        return antSize;
    }
}
