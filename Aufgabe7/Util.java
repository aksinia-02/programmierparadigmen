import java.util.function.Consumer;

public class Util {

    /**
     * Must not be null
     */
    public static Consumer<String> errorHandler = s -> {
        System.err.println(s);
        System.exit(-1);
    };

    // Since we are not allowed to throw exceptions
    public static void panic(String error) {
        errorHandler.accept(error);
    }
}
