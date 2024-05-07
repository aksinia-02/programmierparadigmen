package aufgabe1.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a map of chunks in the game world. Chunks are processed in parallel
 * and are used to generate and manage the game world.
 * STYLE: objected oriented, because it manages many chunk objects and encapsulates access.
 */
public class ChunkMap implements Iterable<Chunk> {
    public static final int MIN_EXPAND_AMOUNT = 2;
    public static final int EDGE_CHUNKS = 2;
    public static final int POPULATED_EDGE_CHUNKS = 1;

    @NotNull
    private final World world;
    /**
     * The world is divided into 3x3 chunks or 9 batches which are each processed in parallel.
     * 9 │7 │8 │9 │7  | The batches 1..9 are processed one after the other.
     * ──┼──┼──┼──┼── | Every chunk in a batch is processed in parallel.
     * 3 │1 │2 │3 │1  | This ensures that threads are unlikely to interfere with each other
     * ──┼──┼──┼──┼── | since the 8 neighboring chunks are only locked if needed (so not initially).
     * 6 │4 │5 │6 │4  |
     * ──┼──┼──┼──┼── |
     * 9 │7 │8 │9 │7  |
     * ──┼──┼──┼──┼── |
     * 3 │1 │2 │3 │1  |
     */
    @NotNull
    private final List<List<Chunk>> batches = List.of(
        new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
        new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
        new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    @NotNull
    private final ChunkCache cache = new ChunkCache();
    @NotNull
    private final WorldGenerator generator;
    @Nullable
    private Chunk @NotNull [] chunks;
    private int offsetX;
    private int offsetY;
    private int sizeX;
    private int sizeY;

    public ChunkMap(@NotNull World world, @NotNull WorldGenerator generator, int initialSize) {
        this.world = world;
        this.generator = generator;

        offsetX = -initialSize / 2;
        offsetY = -initialSize / 2;
        sizeX = initialSize;
        sizeY = initialSize;

        chunks = new Chunk[sizeX * sizeY];
    }

    /**
     * Gets the Chunk at the specified coordinates.
     * The chunk and its neighbors are created if they don't exist yet.
     */
    @NotNull
    public synchronized Chunk get(int chunkX, int chunkY) {
        if (cache.matches(chunkX, chunkY)) return cache.get();

        expandTo(chunkX + EDGE_CHUNKS, chunkY + EDGE_CHUNKS);
        expandTo(chunkX - EDGE_CHUNKS, chunkY - EDGE_CHUNKS);

        int ix = chunkX - offsetX;
        int iy = chunkY - offsetY;
        int index = ix + iy * sizeX;
        Chunk chunk = chunks[index];
        if (chunk == null || !chunk.hasCompleteNeighbors()) {
            chunk = allocateChunks(chunkX, chunkY);

            if (!generator.locked()) {
                generator.setLocked(true);
                try {
                    populateChunks(chunkX, chunkY);
                    chunk.setHasCompleteNeighbors(true);
                } catch (Exception e) {
                    System.err.println("Failed to populate chunks at " + chunk + ": " + e);
                }
                generator.setLocked(false);
            }
        }
        cache.set(chunk);
        return chunk;
    }

    /**
     * Expands the map to include the specified coordinates if necessary.
     */
    private void expandTo(int x, int y) {
        int dx = x - offsetX;
        int dy = y - offsetY;

        int ex = 0, ey = 0;

        if (dx < 0) ex = dx;
        else if (dx >= sizeX) ex = dx - sizeX + 1;
        if (dy < 0) ey = dy;
        else if (dy >= sizeY) ey = dy - sizeY + 1;

        expand(ex, ey);
    }

    /**
     * Allocates a group of neighboring chunks centered at the specified coordinates.
     */
    @NotNull
    private Chunk allocateChunks(int chunkX, int chunkY) {
        Chunk center = null;
        for (int dy = -EDGE_CHUNKS; dy <= EDGE_CHUNKS; dy++) {
            for (int dx = -EDGE_CHUNKS; dx <= EDGE_CHUNKS; dx++) {
                int ix = chunkX + dx - offsetX;
                int iy = chunkY + dy - offsetY;
                int index = ix + iy * sizeX;
                Chunk chunk = chunks[index];
                if (chunk == null) {
                    chunk = new Chunk(world, generator, chunkX + dx, chunkY + dy);
                    chunks[index] = chunk;
                    int batch = (((chunkX + dx) % 3 + 3) % 3) + (((chunkY + dy) % 3 + 3) % 3) * 3;
                    batches.get(batch).add(chunk);
                }
                if (dx == 0 && dy == 0) center = chunk;
            }
        }
        assert center != null;
        return center;
    }

    /**
     * Populates the chunks in the specified area.
     */
    private void populateChunks(int chunkX, int chunkY) {
        for (int dy = -POPULATED_EDGE_CHUNKS; dy <= POPULATED_EDGE_CHUNKS; dy++) {
            for (int dx = -POPULATED_EDGE_CHUNKS; dx <= POPULATED_EDGE_CHUNKS; dx++) {
                int ix = chunkX + dx - offsetX;
                int iy = chunkY + dy - offsetY;
                int index = ix + iy * sizeX;
                Chunk chunk = chunks[index];
                assert chunk != null;
                if (!chunk.populated()) chunk.populate();
            }
        }
    }

    private void expand(int ex, int ey) {
        if (ex == 0 && ey == 0) return;
        if (ex > 0) ex = Math.max(ex, MIN_EXPAND_AMOUNT);
        if (ex < 0) ex = -Math.max(-ex, MIN_EXPAND_AMOUNT);
        if (ey > 0) ey = Math.max(ey, MIN_EXPAND_AMOUNT);
        if (ey < 0) ey = -Math.max(-ey, MIN_EXPAND_AMOUNT);

        int newSizeX = sizeX + Math.abs(ex);
        int newSizeY = sizeY + Math.abs(ey);

        int newOffsetX = offsetX + Math.min(ex, 0);
        int newOffsetY = offsetY + Math.min(ey, 0);

        Chunk[] newChunks = new Chunk[newSizeX * newSizeY];

        for (int newIy = 0; newIy < newSizeY; newIy++) {
            for (int newIx = 0; newIx < newSizeX; newIx++) {
                int newIndex = newIx + newIy * newSizeX;

                int x = newIx + newOffsetX;
                int y = newIy + newOffsetY;

                int oldIx = x - offsetX;
                int oldIy = y - offsetY;

                // inside old region
                if (oldIx >= 0 && oldIy >= 0 && oldIx < sizeX && oldIy < sizeY) {
                    int oldIndex = oldIx + oldIy * sizeX;
                    newChunks[newIndex] = chunks[oldIndex];
                }
            }
        }

        sizeX = newSizeX;
        sizeY = newSizeY;
        offsetX = newOffsetX;
        offsetY = newOffsetY;
        chunks = newChunks;
    }

    /**
     * Gets the Chunk at the specified coordinates, or returns null if the coordinates
     * are outside the map.
     */
    @Nullable
    public synchronized Chunk getOrNull(int chunkX, int chunkY) {
        if (!isInside(chunkX, chunkY)) {
            return null;
        }

        int ix = chunkX - offsetX;
        int iy = chunkY - offsetY;
        return chunks[ix + iy * sizeX];
    }

    /**
     * Checks if the specified coordinates are inside the map.
     */
    private boolean isInside(int x, int y) {
        return x >= offsetX && y >= offsetY && x < offsetX + sizeX && y < offsetY + sizeY;
    }

    @NotNull
    @Override
    public java.util.Iterator<@Nullable Chunk> iterator() {
        return new Iterator(chunks);
    }

    @NotNull
    public List<List<Chunk>> batches() {
        return batches;
    }

    public void setSeed(int seed) {
        generator.setSeed(seed);
    }

    private static class Iterator implements java.util.Iterator<Chunk> {
        private final Chunk[] chunks;
        private int index = 0;

        private Iterator(Chunk[] chunks) {
            this.chunks = chunks;
        }

        @Override
        public boolean hasNext() {
            return index < chunks.length;
        }

        @Override
        public Chunk next() {
            return chunks[index++];
        }
    }

    /**
     * A private nested class for caching the last accessed Chunk to improve performance.
     */
    private static class ChunkCache {
        private Chunk value;
        private int chunkX;
        private int chunkY;

        public boolean matches(int chunkX, int chunkY) {
            return value != null && this.chunkX == chunkX && this.chunkY == chunkY;
        }

        public void set(@NotNull Chunk chunk) {
            this.value = chunk;
            this.chunkX = chunk.chunkX();
            this.chunkY = chunk.chunkY();
        }

        @SuppressWarnings("unused")
        public void set(@NotNull ChunkCache cache) {
            this.value = cache.value;
            this.chunkX = cache.chunkX;
            this.chunkY = cache.chunkY;
        }

        public Chunk get() {
            return value;
        }
    }
}
