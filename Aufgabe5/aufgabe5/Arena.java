package aufgabe5;

import org.jetbrains.annotations.NotNull;

/**
 * A class representing an arena, extending the {@link AbstractPart} class.
 *
 * @see AbstractPart
 */
public class Arena extends AbstractPart {
    private final float volume;

    public Arena(Quality quality, float volume) {
        super(quality);
        this.volume = volume;
    }

    /**
     * Returns the volume of the arena.
     *
     * @return The volume of the arena.
     */
    public float volume() {
        return volume;
    }

    /**
     * Returns the rating of this arena by the specified criteria.
     *
     * @param part The criteria for rating the arena. Must not be null.
     * @return The calculated rating of the arena.
     * @see Rated#rated(Object)
     */
    @Override
    public Quality rated(@NotNull Part part) {
        // https://tuwel.tuwien.ac.at/mod/forum/discuss.php?d=400955#p929912
        return quality.sum(part.rated());
    }

    /**
     * Checks if this arena is equal to another object.
     *
     * @param o The object to compare with this arena.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return quality.equals(((Arena) o).quality) && volume == ((Arena) o).volume;
    }
}
