package aufgabe9;

/**
 * Enum representing directions with associated symbols.
 */
public enum Direction {
    RIGHT(">"),
    LEFT("<"),
    UP("A"),
    DOWN("V");

    private final String symbol;

    /**
     * Constructs a Direction enum with the specified symbol.
     *
     * @param symbol The symbol associated with the direction.
     */
    Direction(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the symbol associated with the direction.
     *
     * @return The symbol of the direction.
     */
    public String symbol() {
        return symbol;
    }
}
