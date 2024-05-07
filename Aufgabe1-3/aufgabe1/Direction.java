package aufgabe1;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The `Direction` enum represents cardinal and ordinal directions in a 2D grid. Each direction is associated
 * with a change in the x (dx) and y (dy) coordinates. This enum provides methods to calculate the direction to
 * the left, right, or opposite of the current direction.
 * Modularisierungseinheit: Modul (objektorientiert)
 */
public enum Direction {
    North(0, -1),
    NorthEast(1, -1),
    East(1, 0),
    SouthEast(1, 1),
    South(0, 1),
    SouthWest(-1, 1),
    West(-1, 0),
    NorthWest(-1, -1);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns a direction based on a 2D vector representation.
     *
     * @param v The 2D vector representing the direction.
     * @return The direction corresponding to the vector.
     */
    @Nullable
    public static Direction fromVector(@NotNull IVector v) {
        return fromDelta(v.x(), v.y());
    }

    /**
     * Returns a direction based on the change in x and y coordinates (dx and dy).
     *
     * @param dx The change in the x-coordinate.
     * @param dy The change in the y-coordinate.
     * @return The direction corresponding to the change in coordinates.
     */
    @Nullable
    public static Direction fromDelta(int dx, int dy) {
        // fast and precise check for small values
        boolean small = Math.abs(dx) <= 1 && Math.abs(dy) <= 1;
        if (small) {
            return switch (dx + (3 * dy)) {
                case -4 -> NorthWest;
                case -3 -> North;
                case -2 -> NorthEast;
                case -1 -> West;
                case 0 -> null;
                case 1 -> East;
                case 2 -> SouthWest;
                case 3 -> South;
                case 4 -> SouthEast;
                default -> throw new IllegalStateException("Impossible value: " + dx + (3 * dy));
            };
        }

        final float SILVER_RATIO = 2.414213562373095f;
        final float INV_SILVER_RATIO = 1.0f / 2.414213562373095f;
        if (dx > 0) {
            if (dy > 0) {
                float ratio = (float) dx / (float) dy;

                if (ratio >= SILVER_RATIO) {
                    return East;
                } else if (ratio < INV_SILVER_RATIO) {
                    return South;
                } else {
                    return SouthEast;
                }
            } else if (dy < 0) {
                float ratio = (float) dx / (float) -dy;

                if (ratio >= SILVER_RATIO) {
                    return East;
                } else if (ratio < INV_SILVER_RATIO) {
                    return North;
                } else {
                    return NorthEast;
                }
            } else {
                return East;
            }
        } else if (dx < 0) {
            if (dy > 0) {
                float ratio = (float) -dx / (float) dy;

                if (ratio >= SILVER_RATIO) {
                    return West;
                } else if (ratio < INV_SILVER_RATIO) {
                    return South;
                } else {
                    return SouthWest;
                }
            } else if (dy < 0) {
                float ratio = (float) -dx / (float) -dy;

                if (ratio >= SILVER_RATIO) {
                    return West;
                } else if (ratio < INV_SILVER_RATIO) {
                    return North;
                } else {
                    return NorthWest;
                }
            } else {
                return West;
            }
        } else {
            if (dy > 0) {
                return Direction.South;
            } else if (dy < 0) {
                return Direction.North;
            }
        }

        return null;
    }

    /**
     * @param count how many to the left
     * @return the direction to the left of the current direction
     */
    public Direction left(int count) {
        Direction[] values = Direction.values();
        return values[(ordinal() + values.length - count) % values.length];
    }

    /**
     * @return the opposite direction of the current direction.
     */
    public Direction opposite() {
        return this.right(4);
    }

    /**
     * @param count how many to the right
     * @return the direction to the right of the current direction
     */
    public Direction right(int count) {
        Direction[] values = Direction.values();
        return values[(ordinal() + values.length + count) % values.length];
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    @NotNull
    public Vector vector(int distance) {
        return new Vector(dx * distance, dy * distance);
    }

    @SuppressWarnings("unused")
    @NotNull
    public Vector vector() {
        return new Vector(dx, dy);
    }
}
