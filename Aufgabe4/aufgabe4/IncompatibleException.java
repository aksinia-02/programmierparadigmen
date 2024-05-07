package aufgabe4;

import java.util.function.Function;

public class IncompatibleException extends RuntimeException {
    public IncompatibleException(String property, Compatability a, Compatability b, Function<Compatability, Float> min, Function<Compatability, Float> max) {
        super(String.format("Incompatible %s range; [%.3f, %.3f] and [%.3f, %.3f]",
                property,
                min.apply(a),
                max.apply(a),
                min.apply(b),
                max.apply(b)));
    }

    public IncompatibleException(String message) {
        super(message);
    }
}
