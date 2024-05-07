package aufgabe4;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Eine Form eines Ameisennests.
 */
public class AntFarm extends Nest {

    private final Substrate substrate;
    private final Material panelMaterial;
    private final float panelDistance;

    /**
     * Precondition: The parameters compatability, substrate, and panelMaterial must not be null.
     * @param compatability The compatibility describing the environmental conditions of the AntFarm.
     * @param substrate The substrate used in the AntFarm.
     * @param panelMaterial The material of the panels used in the AntFarm.
     * @param panelDistance The distance between panels in the AntFarm.
     */
    public AntFarm(Compatability compatability, Substrate substrate, Material panelMaterial, float panelDistance) {
        super(compatability);
        this.substrate = substrate;
        this.panelMaterial = panelMaterial;
        this.panelDistance = panelDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AntFarm antFarm = (AntFarm) o;
        return substrate == antFarm.substrate && panelDistance == antFarm.panelDistance && panelMaterial == antFarm.panelMaterial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), substrate);
    }
}
