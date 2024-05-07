package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * "Eine Arena ist kein Nest, ein Nest keine Arena und eine Arena alleine kein Formicarium."
 */
public class Arena implements FormicariumPart {

    private final Compatability compatability;
    private final Substrate substrate;
    private final Material container;

    /**
     * Precondition: The parameters compatability, substrate, and container must not be null.
     * @param compatability The compatibility describing the environmental conditions of the Arena.
     * @param substrate The substrate used in the Arena.
     * @param container The material of the Arena's container.
     */
    public Arena(Compatability compatability, Substrate substrate, Material container) {
        this.compatability = compatability;
        this.substrate = substrate;
        this.container = container;
    }

    /**
     * Postcondition: compatability is not null
     * @return The compatibility describing the environmental conditions of the Arena.
     */
    @Override
    public Compatability compatability() {
        return compatability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arena arena = (Arena) o;
        return Objects.equals(compatability, arena.compatability) && substrate == arena.substrate && container == arena.container;
    }

    @Override
    public int hashCode() {
        return Objects.hash(compatability, substrate, container);
    }
}
