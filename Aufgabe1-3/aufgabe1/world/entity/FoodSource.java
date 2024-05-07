package aufgabe1.world.entity;

import aufgabe1.world.Cell;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * FoodSource represents a source of food in the simulation. An ant can pick food from the source.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class FoodSource extends Entity {
    // always >= 0
    private float amount;
    private int expireTimer;

    public FoodSource(@NotNull Cell cell, float amount, int expireTime) {
        super(cell);
        increaseAmount(amount);
        this.expireTimer = expireTime;
    }

    /**
     * Increases the amount of food available in the source.
     *
     * @param amount must be positive
     */
    public void increaseAmount(float amount) {
        if (amount < 0) throw new IllegalArgumentException("'amount' must be positive");
        this.amount += amount;
    }

    /**
     * @param amount must be positive and less than the available amount
     */
    public void decreaseAmount(float amount) {
        if (amount < 0) throw new IllegalArgumentException("'amount' must be positive");
        if (amount > this.amount) throw new IllegalArgumentException("'amount' must be less than available");
        this.amount -= amount;
    }

    /**
     * @return the amount of food stored in this source. Always >= 0
     */
    @Contract(pure = true)
    public float amount() {
        return amount;
    }

    /**
     * Checks if the food source is empty.
     *
     * @return True if the source is empty (amount <= 0), false otherwise.
     */
    @Contract(pure = true)
    public boolean isEmpty() {
        return amount <= 0;
    }

    /**
     * Updates the food source.
     * If the source is empty, it disassociates itself from the cell.
     */
    @Override
    public void update() {
        if (expireTimer > 0) {
            expireTimer--;
        } else {
            amount = 0;
        }

        if (amount == 0) {
            cell.setFoodSource(null);
        }
    }
}
