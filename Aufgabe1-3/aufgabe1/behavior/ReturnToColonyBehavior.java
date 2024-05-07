package aufgabe1.behavior;

import aufgabe1.Direction;
import aufgabe1.IVector;
import aufgabe1.Vector;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.Colony;
import aufgabe1.world.entity.PathHistory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * Represents the abstraction of the return to colony behavior of an ant.
 * Modularisierungseinheit: Klasse
 * STYLE: object-oriented, because the class abstracts the behavior of ants returning to their colony by encapsulating
 * relevant data and methods within the class. Also, it implements the behavior class.
 */
public class ReturnToColonyBehavior extends Behavior {
    private ImaginaryAnt imagination;
    @NotNull
    private Vector targetDelta = new Vector();

    protected ReturnToColonyBehavior(Behaviors behaviors) {
        super(behaviors);
    }

    @Override
    @NotNull
    protected Score evaluateDirectionScore(@NotNull WeightedDirection direction) {
        return new Score(mixScents(0, direction.cell().colonyScent(behaviors.ant.colony())));
    }

    /**
     * Called when the behavior changes to this one
     */
    @Override
    public void begin() {
        super.begin();
        PathHistory history = behaviors.ant.pathHistory();
        imagination = new ImaginaryAnt(history.integratePath().iterator());
        imagination.setMark();
        targetDelta = new Vector();
        history.reset();
        behaviors.ant.setRecordPath(false);
    }

    /**
     * Called when the behavior changes to a different one
     */
    @Override
    public void end() {
        super.end();
        behaviors.ant.setRecordPath(true);
    }

    /**
     * Defines the behavior of an ant during its turn.
     */
    @Override
    public void act() {
        super.act();

        Ant ant = behaviors.ant;

        if (ant.cell().colony() == ant.colony()) {
            if (ant.cell().world().isDay()) {
                if (ant.isCarrying()) {
                    Colony colony = ant.cell().colony();
                    assert colony != null;
                    ant.depositAllFood(colony);
                    turnAround();
                    ant.setNextBehavior(ant.behaviors().followScentToFood());
                } else {
                    ant.setNextBehavior(behaviors.exploreInit());
                }
            } else {
                ant.setSleep(1);
            }
            return;
        }

        // 1. Go to colony
        for (WeightedDirection direction : ant.possibleNextCells()) {
            if (direction.cell().colony() == ant.colony()) {
                ant.emitColonyScent();
                ant.setNextDirection(direction.direction());
                return;
            }
        }

        // 2. Go along scent trail
        WeightedDirection bestColonyScentDirection = evaluateBestDirection();
        if (bestColonyScentDirection.cell().colonyScent(ant.colony()) >= behaviors.parameters.highScentThreshold) {
            targetDelta.sub(bestColonyScentDirection.direction());
            ant.setNextDirection(bestColonyScentDirection.direction());
            return;
        }

        // 3. Go along remembered path

        // Reached current target
        if (targetDelta.isZero()) {
            // Nowhere left to go
            if (imagination.finished()) {
                // The ant is completely lost, so just wander around aimlessly
                if (behaviors.random.nextFloat() > 0.01) {
                    if (behaviors.random.nextBoolean()) {
                        ant.setNextDirection(ant.direction().left(1));
                    } else {
                        ant.setNextDirection(ant.direction().right(1));
                    }
                }
                return;
            }

            // Find next target
            while (!imagination.finished() && !imagination.hasImproved()) {
                imagination.step();
            }

            targetDelta = new Vector(imagination.position()).sub(imagination.mark());
            imagination.setMark();
        }

        // Go towards target
        if (!targetDelta.isZero()) {
            Direction direction = Direction.fromVector(targetDelta);
            assert direction != null;
            ant.setNextDirection(direction);
            targetDelta.sub(direction);
        }
    }

    /**
     * Represents an imaginary ant used for simulating path exploration.
     */
    private static class ImaginaryAnt {
        @NotNull
        private final Iterator<PathHistory.IntegratedSegment> path;
        @Nullable
        private Vector pos;
        @Nullable
        private Vector mark;
        @Nullable
        private Direction direction;
        private int remainingSteps;
        private boolean finished = false;

        public ImaginaryAnt(@NotNull Iterator<PathHistory.IntegratedSegment> path) {
            this.path = path;
            finished = !nextSegment();
        }

        /**
         * Moves to the next path segment.
         *
         * @return True if there is a next segment, false otherwise.
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean nextSegment() {
            if (!path.hasNext()) return false;
            PathHistory.IntegratedSegment segment = path.next();
            direction = segment.direction().opposite();
            remainingSteps = segment.distance();
            pos = new Vector(segment.position());
            return true;
        }

        /**
         * Takes a step.
         */
        public void step() {
            if (finished) return;

            if (remainingSteps <= 0) {
                if (!nextSegment()) {
                    finished = true;
                    return;
                }
            }

            assert pos != null && direction != null;
            pos.add(direction);
            remainingSteps--;
        }

        /**
         * Checks if the imaginary ant has finished its path.
         *
         * @return True if the ant has finished, false otherwise.
         */
        public boolean finished() {
            return finished;
        }

        /**
         * Gets the current position of the imaginary ant.
         *
         * @return The current position vector.
         */
        @Nullable
        public IVector position() {
            return pos;
        }

        /**
         * Gets the marked position of the imaginary ant.
         *
         * @return The marked position vector.
         */
        @Nullable
        public IVector mark() {
            return mark;
        }

        /**
         * Sets the mark at the current position of the imaginary ant.
         */
        public void setMark() {
            if (pos == null) {
                mark = null;
            } else {
                mark = pos.copy();
            }
        }

        /**
         * Checks if the imaginary ant's current position is closer to the starting point than the previous position.
         *
         * @return True if the position has improved (closer to the mark), false otherwise.
         */
        public boolean hasImproved() {
            if (mark == null) return true;
            if (pos == null) return false;
            return pos.length() < mark.length();
        }
    }
}
