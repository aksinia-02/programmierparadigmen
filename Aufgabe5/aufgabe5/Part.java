package aufgabe5;
import org.jetbrains.annotations.NotNull;

/**
 * An interface representing a part, extending the {@link Rated} interface.
 * Instances of this interface can be rated for quality based on specific criteria.
 */
public interface Part extends Rated<Part, Quality> {

    /**
     * Returns a non-null string representation of the part.
     *
     * @return The string representation of the part.
     */
    @NotNull
    String toString();
}
