package aufgabe1.world.entity;

import org.jetbrains.annotations.NotNull;

/**
 * Represents parameters for an Ant entity, including its behavior settings.
 * This class is designed for configuring various attributes of an Ant.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class AntParameters {

    @NotNull
    private final AntParameters.Behavior behavior;
    public float carryingCapacity;
    public float straightBias;
    public float colonyScentAddend;
    public float colonyScentDecay;
    public float foodScentAddend;
    public float foodScentDecay;
    public float colonyScentGain;
    public float foodScentGain;
    public float avoidScentAdded;
    public float avoidScentFactor;
    public int energyGain;
    public float energyFoodFactor;


    public AntParameters() {
        behavior = new Behavior();
    }

    /**
     * Copy constructor for AntParameters, creating a deep copy of the provided instance.
     *
     * @param other The AntParameters instance to copy.
     */
    public AntParameters(@NotNull AntParameters other) {
        this.carryingCapacity = other.carryingCapacity;
        this.straightBias = other.straightBias;
        this.colonyScentAddend = other.colonyScentAddend;
        this.colonyScentDecay = other.colonyScentDecay;
        this.foodScentAddend = other.foodScentAddend;
        this.foodScentDecay = other.foodScentDecay;
        this.colonyScentGain = other.colonyScentGain;
        this.foodScentGain = other.foodScentGain;
        this.avoidScentAdded = other.avoidScentAdded;
        this.avoidScentFactor = other.avoidScentFactor;
        this.energyGain = other.energyGain;
        this.energyFoodFactor = other.energyFoodFactor;

        // IntelliJ is a little stupid and complains that "Copy constructor does not copy all fields"
        // I don't want to suppress the warning all together, this workaround fixes it.
        //noinspection ConstantValue
        if (true) {
            //noinspection IncompleteCopyConstructor
            this.behavior = new Behavior(other.behavior);
        } else {
            this.behavior = other.behavior;
        }
    }

    @NotNull
    public AntParameters.Behavior behavior() {
        return behavior;
    }

    /**
     * Represents the behavior settings for an Ant entity.
     */
    public static class Behavior {
        public float highScentThreshold;
        public float badScentFollowTimeThreshold;
        public float weightAvoid;
        public float weightPursue;
        public float weightHeight;
        public float biasMix;
        public float weightScore;
        public float weightRandom;
        public float weightStraight;
        public float biasChoose;
        public float argRandom;

        private Behavior() {
        }

        public Behavior(@NotNull Behavior other) {
            // GOOD: Inner class for behavior settings is appropriately encapsulated.
            this.highScentThreshold = other.highScentThreshold;
            this.badScentFollowTimeThreshold = other.badScentFollowTimeThreshold;
            this.weightAvoid = other.weightAvoid;
            this.weightPursue = other.weightPursue;
            this.biasMix = other.biasMix;
            this.weightScore = other.weightScore;
            this.weightRandom = other.weightRandom;
            this.weightStraight = other.weightStraight;
            this.biasChoose = other.biasChoose;
            this.argRandom = other.argRandom;
            this.weightHeight = other.weightHeight;
        }
    }
}