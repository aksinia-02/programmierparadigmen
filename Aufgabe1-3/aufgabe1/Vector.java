package aufgabe1;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a vector with x and y variables
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Vector implements IVector {
    public int x = 0;
    public int y = 0;

    public Vector(@Nullable IVector vector) {
        if (vector != null) {
            this.x = vector.x();
            this.y = vector.y();
        }
    }

    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector() {

    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @NotNull
    public Vector set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @NotNull
    public Vector set(@Nullable IVector v) {
        if (v != null) {
            this.x = v.x();
            this.y = v.y();
        }
        return this;
    }

    @NotNull
    public Vector add(@Nullable IVector v) {
        if (v != null) {
            x += v.x();
            y += v.y();
        }
        return this;
    }

    @NotNull
    public Vector add(int dx, int dy) {
        x += dx;
        y += dy;
        return this;
    }

    @NotNull
    public Vector add(@Nullable Direction d) {
        if (d != null) {
            x += d.dx();
            y += d.dy();
        }
        return this;
    }

    @NotNull
    public Vector sub(@Nullable IVector v) {
        if (v != null) {
            x -= v.x();
            y -= v.y();
        }
        return this;
    }

    @NotNull
    public Vector sub(int dx, int dy) {
        x -= dx;
        y -= dy;
        return this;
    }

    @NotNull
    public Vector sub(@Nullable Direction d) {
        if (d != null) {
            x -= d.dx();
            y -= d.dy();
        }
        return this;
    }

    @NotNull
    public Vector mul(int distance) {
        x *= distance;
        y *= distance;
        return this;
    }

    @NotNull
    public Vector copy() {
        return new Vector(this);
    }
}
