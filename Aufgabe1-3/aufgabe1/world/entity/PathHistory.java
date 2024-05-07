package aufgabe1.world.entity;

import aufgabe1.Direction;
import aufgabe1.IVector;
import aufgabe1.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PathHistory {
    private final int @NotNull [] heads;
    @NotNull
    private final List<List<Segment>> levels;
    private final int partitionSize;

    public PathHistory(int partitionSize, int levels) {
        // the number of saved steps is roughly 2^levels * partitionSize
        this.partitionSize = partitionSize;
        List<Segment> contiguous = Arrays.asList(new Segment[partitionSize * levels]);
        this.levels = new ArrayList<>(levels);
        this.heads = new int[levels];
        for (int lod = 0; lod < levels; lod++) {
            this.levels.add(contiguous.subList(lod * partitionSize, (lod + 1) * partitionSize));
        }
    }

    public void push(@NotNull Direction step) {
        push(new Segment(step, 1), 0);
    }

    public void reset() {
        for (int lod = 0; lod < levels.size(); lod++) {
            heads[lod] = 0;
            Collections.fill(levels.get(lod), null);
        }
    }

    @NotNull
    public List<IntegratedSegment> integratePath() {
        List<IntegratedSegment> path = new ArrayList<>(partitionSize * levels.size());
        Vector pos = new Vector();
        for (int lod = levels.size() - 1; lod >= 0; lod--) {
            List<Segment> level = levels.get(lod);
            for (int offset = 0; offset < level.size(); offset++) {
                // start at oldest
                int index = (heads[lod] + offset) % partitionSize;
                Segment s = level.get(index);
                if (s == null) continue;

                pos.add(s.direction.vector(s.distance));
                path.add(new IntegratedSegment(s.direction, s.distance, pos.copy()));
            }
        }
        Collections.reverse(path);
        return path;
    }

    private void push(@NotNull Segment carry, int lod) {
        if (lod >= levels.size()) return;

        List<Segment> level = levels.get(lod);
        int head = heads[lod];
        int next = (head + 1) % partitionSize;

        Segment oldest = level.get(head);

        // enqueue
        level.set(head, carry);
        heads[lod] = next;

        // the last level can't carry over and the oldest just gets overwritten
        if (oldest != null && lod < levels.size() - 1) {
            Segment nextOldest = level.get(next);
            // dequeue
            carry = merge(oldest, nextOldest);
            level.set(next, null);
            if (carry != null) {
                push(carry, lod + 1);
            }
        }
    }

    @Nullable
    private Segment merge(@NotNull Segment a, @NotNull Segment b) {
        int dx = a.direction.dx() * a.distance + b.direction.dx() * b.distance;
        int dy = a.direction.dy() * a.distance + b.direction.dy() * b.distance;
        if (dx == 0 && dy == 0) return null;

        Direction direction = Direction.fromDelta(dx, dy);
        int distance = Math.max(Math.abs(dx), Math.abs(dy));
        return new Segment(direction, distance);
    }


    public record IntegratedSegment(Direction direction, int distance, IVector position) {
    }

    public record Segment(Direction direction, int distance) {
    }
}
