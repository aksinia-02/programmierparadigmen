package aufgabe1.view;

import aufgabe1.IVector;
import aufgabe1.Simulation;
import aufgabe1.world.Cell;
import aufgabe1.world.Chunk;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.Colony;
import aufgabe1.world.entity.FoodSource;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

/**
 * A class representing a visualization of the simulation world.
 * It provides a visual representation of the world, including various colors for the different elements.
 * Modularisierungseinheit: Klasse
 */
public class Visualization extends JComponent {
    @NotNull
    private final Simulation simulation;
    private final int widthPx;
    private final int heightPx;
    private final WeakHashMap<Colony, ColonyColors> colonyColors = new WeakHashMap<>();
    private final int foodColor;
    private final List<Map.Entry<Float, Integer>> terrainColors = List.of(
        Map.entry(0.5f, new Color(143, 217, 143).getRGB()),
        Map.entry(0.62f, new Color(91, 145, 91).getRGB()),
        Map.entry(0.7f, new Color(150, 149, 149).getRGB()),
        Map.entry(1.0f, new Color(218, 218, 218).getRGB())
    );
    @NotNull
    private Parameters parameters;
    @NotNull
    private BufferedImage drawImage;
    @NotNull
    private BufferedImage readImage;
    private int @NotNull [] pixels;

    public Visualization(@NotNull Simulation simulation, @NotNull Parameters parameters, int widthPx, int heightPx) {
        Objects.requireNonNull(simulation);
        Objects.requireNonNull(parameters);
        this.simulation = simulation;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.parameters = parameters;

        drawImage = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);
        drawImage.setAccelerationPriority(1);
        pixels = ((DataBufferInt) drawImage.getRaster().getDataBuffer()).getData();
        readImage = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);
        readImage.setAccelerationPriority(1);

        foodColor = new Color(0, 1, 0.2f).getRGB();
    }

    private void swapImages() {
        BufferedImage tmp = drawImage;
        drawImage = readImage;
        readImage = tmp;

        pixels = ((DataBufferInt) drawImage.getRaster().getDataBuffer()).getData();
    }

    /**
     * Paints scent, colonies, ants and food in this visualization immediately.
     */
    @Override
    public void paint(Graphics g) {
        boolean completed = paintImmediate();

        if (completed) {
            swapImages();
        }

        Graphics2D g2d = (Graphics2D) g;
        // I'm not sure if setting these hints actually does anything
        configureGraphicsRenderingHints(g2d);
        g2d.drawImage(readImage, 0, 0, null);
        g2d.dispose();

        synchronized (this) {
            notify();
        }
    }

    /**
     * STYLE: functional
     * This method promotes referential transparency by succinctly encapsulating
     * the rendering hints within a Map and applying them to the Graphics2D object, ensuring clarity and ease of
     * understanding regarding the applied rendering settings.
     */
    private void configureGraphicsRenderingHints(@NotNull Graphics2D g2d) {
        Map.of(
            RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED,
            RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED,
            RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED,
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF
        ).forEach(g2d::setRenderingHint);
    }

    @NotNull
    private ColonyColors getColors(@NotNull Colony colony) {
        return colonyColors.computeIfAbsent(colony, k -> new ColonyColors());
    }

    private boolean paintImmediate() {
        if (parameters.scale == 0) return false;
        clear(0xff808080);

        boolean completed;
        try {
            completed = paintWorld();
            if (parameters.inspectMode) paintInspection();
        } catch (Exception ignored) {
            // various exceptions may occur due to multithreading, but it doesn't really matter
            return false;
        }
        return completed;
    }

    /**
     * STYLE: functional
     */
    private void paintInspection() {
        Point mouse = getMousePosition();
        if (mouse == null) return;

        Cell cell = simulation.world().getOrNull(pxToCellX(mouse.x), pxToCellY(mouse.y));
        if (cell == null) return;

        List<String> lines = Stream.of(
            cell.ants().stream().map(ant -> String.format("Ant: %s", ant.uuid())),
            Stream.of(cell.foodSource()).takeWhile(Objects::nonNull)
                .map(source -> String.format("Food: %.1f", source.amount())),
            Stream.of(cell.colony()).takeWhile(Objects::nonNull)
                .map(colony -> String.format("Colony: %s", colony.uuid()))
        ).reduce(Stream::concat).orElseGet(Stream::empty).toList();

        Graphics2D g = drawImage.createGraphics();
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        Point at = new Point(mouse);
        at.x += 12;
        Rectangle2D rect = lines.stream().map(s -> fm.getStringBounds(s, g)).reduce(new Rectangle2D.Float(), (a, b) -> {
            a.add(b.getWidth(), 16 + b.getHeight());
            return a;
        });
        g.setPaint(Color.WHITE);
        g.fillRect(at.x - 4,
            at.y - fm.getAscent(),
            (int) rect.getWidth() + 8,
            (int) rect.getHeight());

        g.setPaint(Color.BLACK);
        lines.forEach(line -> {
            g.drawString(line, at.x, at.y);
            at.y += 16;
        });
        g.dispose();
    }


    private boolean paintWorld() {
        List<Chunk> visible = new ArrayList<>();
        int width = widthPx / (parameters.scale * Chunk.CHUNK_SIZE) + 2;
        int height = heightPx / (parameters.scale * Chunk.CHUNK_SIZE) + 2;
        // STYLE: Parallel
        // Locking the world ensures that getChunk calls will not have to wait.
        // Since the lock is not fair a timeout is specified.
        // If the timeout is reached the visualization is not updated and the last image gets reused.
        // The world is only locked during the initial visibility test to avoid a long pause
        // while the image gets painted.
        // Since the simulation performance is more important than visualization accuracy it was decided to
        // let the simulation run in parallel while the visualization is still accessing data.
        // This leads to inconsistencies while painting and occasionally to exceptions. However, this is by design
        // and not a problem as it simply skips a frame.
        if (!simulation.world().lock(100)) {
            // lock world with timeout to avoid unresponsive UI
            return false;
        }
        for (int dx = -1; dx < width; dx++) {
            for (int dy = -1; dy < height; dy++) {
                int x = dx + parameters.centerX / (parameters.scale * Chunk.CHUNK_SIZE);
                int y = dy + parameters.centerY / (parameters.scale * Chunk.CHUNK_SIZE);
                Chunk chunk = simulation.world().getChunkOrNull(x, y);

                if (chunk == null) continue;

                visible.add(chunk);
            }
        }
        simulation.world().unlock();

        // paint background
        for (Chunk chunk : visible) {
            paintChunk(chunk);
        }

        // paint over background
        for (Chunk chunk : visible) {
            paintChunkContent(chunk);
        }

        return true;
    }

    private void paintChunk(@NotNull Chunk chunk) {
        int x = chunk.chunkX() * Chunk.CHUNK_SIZE;
        int y = chunk.chunkY() * Chunk.CHUNK_SIZE;
        if (chunk.populated()) {
            paintHeight(chunk);
        } else {
            fillRect(0xffc0c0c0, 0xff, x, y, Chunk.CHUNK_SIZE, Chunk.CHUNK_SIZE);
        }

        if (parameters.chunkBorders) {
            fillRect(0xff000000, 0x10, x, y, Chunk.CHUNK_SIZE, 1);
            fillRect(0xff000000, 0x10, x, y, 1, Chunk.CHUNK_SIZE);

            Graphics2D g = drawImage.createGraphics();
            g.setPaint(Color.BLACK);
            String str = chunk.chunkX() + ", " + chunk.chunkY() + ": " + chunk.suspendState().name().charAt(0);
            g.drawString(str, cellToPxX(x + 2), cellToPxY(y + Chunk.CHUNK_SIZE - 2));
            g.dispose();
        }
    }

    /**
     * STYLE: Functional
     */
    private void paintHeight(@NotNull Chunk chunk) {
        for (int y = chunk.origin().y(); y < chunk.limit().y(); y++) {
            for (int x = chunk.origin().x(); x < chunk.limit().x(); x++) {
                Cell cell = chunk.get(x, y);
                float cellHeight = cell.height();

                if (parameters.showHeight) {
                    int color = (int) (cellHeight * 0xff);
                    fillRect(packColor(color, color, color, 0xff), 0xff, x, y);
                } else {
                    int finalX = x;
                    int finalY = y;
                    terrainColors.stream()
                        .filter(entry -> cellHeight <= entry.getKey())
                        .findFirst()
                        .ifPresent(entry -> fillRect(entry.getValue(), 0xff, finalX, finalY));
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private int packColor(int r, int g, int b, int a) {
        return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }

    /**
     * STYLE: functional programming
     */
    private void paintChunkContent(@NotNull Chunk chunk) {
        for (Cell cell : chunk) {
            if (cell.isEmpty()) continue;
            try {
                cell.scents().forEach(entry -> {
                    Colony colony = entry.getKey();
                    float foodScent = cell.foodScent(colony);
                    float colonyScent = cell.colonyScent(colony);
                    float avoidScent = cell.avoidScent(colony);

                    if (foodScent <= 0.01 && colonyScent <= 0.01 && avoidScent <= 0.01) return;

                    ColonyColors colors = getColors(colony);

                    int colonyFoodColor = mix(
                        colors.foodScent, toAlpha(foodScent, parameters.scentColorScale),
                        colors.colonyScent, toAlpha(colonyScent, parameters.scentColorScale));

                    fillRect(colonyFoodColor, colonyFoodColor >> 24, cell.position());
                    fillRect(colors.avoidScent, toAlpha(avoidScent, parameters.scentColorScale), cell.position());
                });
            } catch (NoSuchElementException ignored) {
            }
        }
        try {
            chunk.entities().forEach(entity -> {
                if (entity instanceof Ant) {
                    paintAnt((Ant) entity);
                } else if (entity instanceof Colony) {
                    paintColony((Colony) entity);
                } else if (entity instanceof FoodSource) {
                    paintFood((FoodSource) entity);
                }
            });
        } catch (NoSuchElementException ignored) {
        }
    }

    /**
     * STYLE: functional programming
     */
    private void paintAnt(@NotNull Ant ant) {
        int antPadding = Math.round(parameters.scale * 0.25f);
        int color = ant.carrying() > 0 ? Color.GREEN.getRGB() : Color.BLACK.getRGB();
        fillRect(color, 0xff, ant.position(), antPadding);
    }

    /**
     * STYLE: functional programming
     */
    private void paintColony(@NotNull Colony colony) {
        ColonyColors colors = getColors(colony);
        colony.cells().forEach(cell -> fillRect(colors.colony, 0x7f, cell.position()));
    }

    private void paintFood(@NotNull FoodSource food) {
        int alpha = (int) (0.5 * toAlpha(food.amount(), parameters.foodColorScale));
        fillRect(foodColor, alpha, food.position());
    }

    private int pxToCellX(int x) {
        return (x + parameters.centerX) / parameters.scale;
    }

    private int pxToCellY(int y) {
        return (y + parameters.centerY) / parameters.scale;
    }

    private int cellToPxX(int x) {
        return x * parameters.scale - parameters.centerX;
    }

    private int cellToPxY(int y) {
        return y * parameters.scale - parameters.centerY;
    }


    /**
     * Converts the value and scale to alpha.
     */
    private int toAlpha(float value, float scale) {
        if (Float.isInfinite(value)) return 0xff;
        return (int) ((value / (value + scale)) * 0xff);
    }

    @SuppressWarnings("SameParameterValue")
    private void fillRect(int color, int alpha, int x, int y, int w, int h) {
        fillRect(color, alpha, x, y, w, h, 0);
    }

    @SuppressWarnings("SameParameterValue")
    private void fillRect(int color, int alpha, @NotNull IVector pos) {
        fillRect(color, alpha, pos.x(), pos.y(), 1, 1, 0);
    }

    @SuppressWarnings("SameParameterValue")
    private void fillRect(int color, int alpha, @NotNull IVector pos, int padding) {
        fillRect(color, alpha, pos.x(), pos.y(), 1, 1, padding);
    }

    @SuppressWarnings("SameParameterValue")
    private void fillRect(int color, int alpha, int x, int y) {
        fillRect(color, alpha, x, y, 1, 1, 0);
    }

    /**
     * Fills a rectangle with the specified color.
     */
    private void fillRect(int color, int alpha, int x, int y, int w, int h, int padding) {
        if (alpha == 0x00) return;
        int scale = parameters.scale;
        padding = Math.min(padding, Math.max(scale - 2 * padding, 0));
        x = x * scale + padding;
        y = y * scale + padding;
        w = w * scale - padding;
        h = h * scale - padding;
        for (int px = x; px < x + w; px++) {
            for (int py = y; py < y + h; py++) {
                int rx = px - parameters.centerX;
                int ry = py - parameters.centerY;
                if (rx < 0 || rx >= widthPx || ry < 0 || ry >= heightPx) continue;
                int i = rx + ry * widthPx;
                pixels[i] = blend(pixels[i], color, alpha);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void clear(int color) {
        Arrays.fill(pixels, color);
    }

    /**
     * Mixes two colors.
     * Does not support transparent background colors.
     */
    @SuppressWarnings("DuplicatedCode")
    private int blend(int backgroundColor, int foregroundColor, int alpha) {
        int a = alpha & 0xff;
        int a_prime = 0xff - a;

        if (a_prime == 0x00) return foregroundColor;

        int ad = (backgroundColor >> 24) & 0xff;
        int rd = (backgroundColor >> 16) & 0xff;
        int gd = (backgroundColor >> 8) & 0xff;
        int bd = backgroundColor & 0xff;

        int rs = (foregroundColor >> 16) & 0xff;
        int gs = (foregroundColor >> 8) & 0xff;
        int bs = foregroundColor & 0xff;

        int rc = (rs * a + rd * a_prime) / 0xff;
        int gc = (gs * a + gd * a_prime) / 0xff;
        int bc = (bs * a + bd * a_prime) / 0xff;

        int ac = a + (ad * a_prime) / 0xff;

        return (ac & 0xff) << 24 | (rc & 0xff) << 16 | (gc & 0xff) << 8 | (bc & 0xff);
    }

    @SuppressWarnings("DuplicatedCode")
    private int mix(int colorA, int alphaA, int colorB, int alphaB) {
        int sum = (alphaA + alphaB);

        if (sum == 0) return 0x0;

        int pa = (alphaA * 0xff) / sum;
        int pb = (alphaB * 0xff) / sum;

        int ra = (colorA >> 16) & 0xff;
        int ga = (colorA >> 8) & 0xff;
        int ba = colorA & 0xff;

        int rb = (colorB >> 16) & 0xff;
        int gb = (colorB >> 8) & 0xff;
        int bb = colorB & 0xff;

        int rc = (ra * pa + rb * pb) / 0xff;
        int gc = (ga * pa + gb * pb) / 0xff;
        int bc = (ba * pa + bb * pb) / 0xff;

        int ac = Math.max(alphaA, alphaB);

        return (ac & 0xff) << 24 | (rc & 0xff) << 16 | (gc & 0xff) << 8 | (bc & 0xff);
    }

    @NotNull
    public Parameters parameters() {
        return parameters;
    }

    @SuppressWarnings("unused")
    public void setParameters(@NotNull Parameters parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
    }


    /**
     * Modularisierungseinheit: Klasse
     */
    public static class Parameters {
        public float scentColorScale;
        public float foodColorScale;
        public int centerX;
        public int centerY;
        public int scale;
        public boolean chunkBorders;
        public boolean inspectMode;
        public boolean showHeight;
    }

    private static class ColonyColors {
        private static final Random random = new Random(1337);
        private final int colony;
        private final int foodScent;
        private final int colonyScent;
        private final int avoidScent;

        public ColonyColors() {
            float hue = random.nextFloat();
            colony = Color.getHSBColor(hue, 0.6f, 0.7f).getRGB();
            colonyScent = Color.getHSBColor(hue, 0.6f, 0.95f).getRGB();
            foodScent = Color.getHSBColor(hue + 0.5f, 0.6f, 0.95f).getRGB();
            avoidScent = new Color(255, 0, 0).getRGB();
        }
    }
}
