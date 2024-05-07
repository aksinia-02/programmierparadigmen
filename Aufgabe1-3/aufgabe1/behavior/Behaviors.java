package aufgabe1.behavior;

import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.AntParameters;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Contains instances of different behaviors.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented
 */
public class Behaviors {

    @NotNull
    protected final Ant ant;
    @NotNull
    protected final RandomGenerator random;
    @NotNull
    private final Behavior explore;
    @NotNull
    private final Behavior exploreInit;
    @NotNull
    private final Behavior followScentToFood;
    @NotNull
    private final Behavior followScentToColony;
    @NotNull
    private final Behavior returnHome;
    @NotNull
    protected AntParameters.Behavior parameters;

    public Behaviors(@NotNull Ant ant, @NotNull RandomGenerator random, @NotNull AntParameters.Behavior parameters) {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(ant);
        Objects.requireNonNull(random);
        this.parameters = parameters;
        this.ant = ant;
        this.random = random;

        explore = new ExploreBehavior(this);
        exploreInit = new ExploreInitBehavior(this);
        followScentToFood = new FollowScentToFoodBehavior(this);
        followScentToColony = new FollowScentToColonyBehavior(this);
        returnHome = new ReturnToColonyBehavior(this);
    }

    /**
     * @return The behavior for initializing exploration.
     */
    @NotNull
    public Behavior exploreInit() {
        return exploreInit;
    }

    /**
     * @return The behavior for exploration.
     */
    @NotNull
    public Behavior explore() {
        return explore;
    }

    /**
     * @return The behavior for following the scent trail to food.
     */
    @NotNull
    public Behavior followScentToFood() {
        return followScentToFood;
    }

    /**
     * @return The behavior for following the scent trail to the colony.
     */
    @NotNull
    public Behavior followScentToNest() {
        return followScentToColony;
    }

    /**
     * @return The behavior for returning home.
     */
    @NotNull
    public Behavior returnHome() {
        return returnHome;
    }

    /**
     * @return The behavior parameters.
     */
    @NotNull
    public AntParameters.Behavior parameters() {
        return parameters;
    }

    /**
     * Sets the behavior parameters for the ant.
     *
     * @param parameters The behavior parameters to be set.
     */
    public void setParameters(@NotNull AntParameters.Behavior parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
    }
}
