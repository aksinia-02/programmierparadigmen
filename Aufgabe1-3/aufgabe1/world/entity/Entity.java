package aufgabe1.world.entity;

import aufgabe1.IVector;
import aufgabe1.world.Cell;
import aufgabe1.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Abstract class representing an entity in the simulation world.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public abstract class Entity {
    private final String uuid = generateUUID();
    @NotNull
    protected Cell cell;
    @NotNull
    protected final World world;

    public Entity(@NotNull Cell cell) {
        Objects.requireNonNull(cell);
        this.cell = cell;
        this.world = cell.world();
    }

    /**
     * Generates a unique identifier for the entity.
     *
     * @return The UUID of the entity.
     */
    private synchronized static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Retrieves the position vector of the entity.
     *
     * @return The position vector of the entity.
     */
    @NotNull
    @Contract(pure = true)
    public IVector position() {
        return cell.position();
    }

    /**
     * Retrieves the cell in which the entity is located.
     *
     * @return The cell of the entity.
     */
    @NotNull
    @Contract(pure = true)
    public Cell cell() {
        return cell;
    }

    /**
     * Retrieves the UUID of the entity.
     *
     * @return The UUID of the entity.
     */
    public String uuid() {
        return uuid;
    }

    /**
     * Updates the entity. Subclasses can override this method to define specific behaviors.
     */
    public void update() {
    }
}
