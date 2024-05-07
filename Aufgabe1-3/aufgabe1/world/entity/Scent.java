package aufgabe1.world.entity;

import org.jetbrains.annotations.Contract;

public class Scent {
    public static final float NO_SCENT_THRESHOLD = 0.001f;
    public float avoid;
    public float colony;
    public float food;

    @Contract(pure = true)
    public boolean isEmpty() {
        return avoid < NO_SCENT_THRESHOLD && colony < NO_SCENT_THRESHOLD && food < NO_SCENT_THRESHOLD;
    }
}
