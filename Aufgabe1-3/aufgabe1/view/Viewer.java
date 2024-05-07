package aufgabe1.view;

import aufgabe1.Simulation;
import aufgabe1.world.WorldParameters;
import aufgabe1.world.entity.Ant;
import aufgabe1.world.entity.AntParameters;
import aufgabe1.world.entity.Colony;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class representing the viewer for the ant simulation.
 * It provides a graphical user interface to interact with the simulation.
 * Modularisierungseinheit: Klasse
 */
public class Viewer extends JFrame {
    @NotNull
    private final Simulation simulation;
    @NotNull
    private final Visualization visualization;
    @NotNull
    private final JLabel scoreLabel;
    @NotNull
    private final JLabel timeLabel;
    @NotNull
    private final JLabeledSlider delaySlider;
    @NotNull
    private final AtomicLong delayMsAtomic = new AtomicLong();
    @NotNull
    private final AtomicBoolean pausedAtomic = new AtomicBoolean();
    @NotNull
    private final AtomicInteger skipStepAtomic = new AtomicInteger();
    @NotNull
    private final AtomicBoolean windowOpen = new AtomicBoolean();
    private float msPerTick;
    private long lastTpsMeasureTime = System.currentTimeMillis();
    private int lastTpsMeasureTicks;
    @NotNull
    private Colony primaryColony;

    /**
     * Creates a Viewer for the given simulation and creates sliders for all parameters
     */
    public Viewer(@NotNull Simulation simulation, @NotNull Visualization.Parameters visualizationParameters, int widthPx, int heightPx) {
        this.simulation = simulation;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            System.err.println("Preferred Look & Feel not available");
            // If the L&F is not available, it doesn't really matter
        }

        JPanel visualizationPanel = new JPanel();
        visualizationPanel.setLayout(new FlowLayout());
        add(visualizationPanel, BorderLayout.CENTER);

        visualization = new Visualization(simulation, visualizationParameters, widthPx, heightPx);
        visualization.setPreferredSize(new Dimension(widthPx, heightPx));
        makeVisualizationDraggable();
        visualizationPanel.add(visualization);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout());
        add(infoPanel, BorderLayout.PAGE_START);

        scoreLabel = new JLabel();
        infoPanel.add(scoreLabel);

        timeLabel = new JLabel();
        infoPanel.add(timeLabel);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        JScrollPane settingsScrollPane = new JScrollPane(settingsPanel);
        settingsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        settingsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        settingsScrollPane.setBorder(null);
        settingsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(settingsScrollPane, BorderLayout.LINE_END);

        JPanel buttonPanel = createButtonPanel(simulation);
        settingsPanel.add(buttonPanel);
        addHorizontalSeparator(settingsPanel);

        JComponent databaseButtonPanel = createDatabasePanel(simulation);
        settingsPanel.add(databaseButtonPanel);
        addHorizontalSeparator(settingsPanel);

        delaySlider = new JLabeledFloatSlider.Builder().label("Delay").range(0, 0.1f).scale(1000)
            .binding(0, this::setDelay).build();

        JComponent vPanel = createVisualizationParametersPanel(visualizationParameters);
        settingsPanel.add(vPanel);
        addHorizontalSeparator(settingsPanel);

        primaryColony = simulation.world().colonies().get(0);
        AntParameters antParameters = primaryColony.antSpawnParameters();
        Colony.Parameters colonyParameters = primaryColony.parameters();
        AntParameters.Behavior behaviorParameters = antParameters.behavior();

        JComponent behaviorParametersPanel = createBehaviorParametersPanel(simulation, behaviorParameters);
        settingsPanel.add(behaviorParametersPanel);
        addHorizontalSeparator(settingsPanel);

        JComponent antParametersPanel = createAntParametersPanel(simulation, antParameters);
        settingsPanel.add(antParametersPanel);
        addHorizontalSeparator(settingsPanel);

        JComponent colonyParametersPanel = createColonyParametersPanel(simulation, colonyParameters);
        settingsPanel.add(colonyParametersPanel);
        addHorizontalSeparator(settingsPanel);

        JComponent worldParametersPanel = createWorldParametersPanel(simulation.world().parameters());
        settingsPanel.add(worldParametersPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                windowOpen.set(false);
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        windowOpen.set(true);
    }

    private void makeVisualizationDraggable() {
        final Point[] dragPos = {null};
        visualization.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        visualization.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(@NotNull MouseEvent e) {
                dragPos[0] = e.getPoint();
            }
        });
        visualization.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(@NotNull MouseEvent e) {
                if (dragPos[0] == null) return;
                visualization.parameters().centerX += dragPos[0].x - e.getX();
                visualization.parameters().centerY += dragPos[0].y - e.getY();
                dragPos[0] = e.getPoint();
            }
        });
    }

    @NotNull
    private JPanel createButtonPanel(@NotNull Simulation simulation) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setPreferredSize(new Dimension(0, 20));
        buttonPanel.setAlignmentX(0);
        JButton resetButton = createResetButton(simulation);
        buttonPanel.add(resetButton);
        buttonPanel.setPreferredSize(new Dimension(0, resetButton.getPreferredSize().height));

        addVerticalSeparator(buttonPanel);

        JButton pauseButton = new JButton("⏯");
        pauseButton.addActionListener(e -> pausedAtomic.set(!pausedAtomic.get()));
        buttonPanel.add(pauseButton);

        JButton stepButton = new JButton("⏭");
        stepButton.addActionListener(e -> {
            pausedAtomic.set(true);
            skipStepAtomic.incrementAndGet();
        });
        buttonPanel.add(stepButton);
        return buttonPanel;
    }

    /**
     * Adds a horizontal separator to the specified parent
     */
    private static void addHorizontalSeparator(@NotNull JComponent parent) {
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(0, 10));
        parent.add(separator);
        separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        parent.add(separator);
    }

    @NotNull
    private JComponent createDatabasePanel(@NotNull Simulation simulation) {
        JPanel databaseButtonPanel = new JPanel();
        databaseButtonPanel.setLayout(new BoxLayout(databaseButtonPanel, BoxLayout.X_AXIS));
        databaseButtonPanel.setPreferredSize(new Dimension(0, 20));
        databaseButtonPanel.setAlignmentX(0);

        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        saveButton.addActionListener(e -> simulation.saveWorldDataFunctional());
        loadButton.addActionListener(e -> simulation.loadWorldDataFunctional());
        databaseButtonPanel.add(saveButton);
        databaseButtonPanel.add(loadButton);
        databaseButtonPanel.setPreferredSize(new Dimension(0, saveButton.getPreferredSize().height));
        return databaseButtonPanel;
    }

    /**
     * Sets the delay for simulation updates in seconds
     */
    public void setDelay(double delaySeconds) {
        final long delayMs = Math.round(delaySeconds * 1e3);
        delayMsAtomic.set(delayMs);
        delaySlider.setValue((int) delayMs);
    }

    @NotNull
    private JComponent createVisualizationParametersPanel(Visualization.@NotNull Parameters params) {
        return new JOptionGroup.Builder()
            .add(delaySlider)
            .add(new JLabeledSlider.Builder().label("Scale").range(1, 8)
                .binding(params.scale, v -> params.scale = v).build())
            .add(new JLabeledSlider.Builder().label("Chunk Borders").range(0, 1)
                .binding(params.chunkBorders ? 1 : 0, v -> params.chunkBorders = v == 1).build())
            .add(new JLabeledSlider.Builder().label("Show Height").range(0, 1)
                .binding(params.showHeight ? 1 : 0, v -> params.showHeight = v == 1).build())
            .add(new JLabeledSlider.Builder().label("Inspect").range(0, 1)
                .binding(params.inspectMode ? 1 : 0, v -> {
                    if (v == 1) {
                        visualization.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        params.inspectMode = true;
                    } else {
                        visualization.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        params.inspectMode = false;
                    }
                }).build())
            .build();
    }

    @NotNull
    private JComponent createBehaviorParametersPanel(@NotNull Simulation simulation, AntParameters.@NotNull Behavior params) {
        return new JOptionGroup.Builder()
            .label("Behavior Parameters")
            .add(new JLabeledFloatSlider.Builder().label("High Threshold").range(0, 10).scale(100)
                .binding(params.highScentThreshold, v -> params.highScentThreshold = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Pursue").range(0, 10).scale(100)
                .binding(params.weightPursue, v -> params.weightPursue = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Avoid").range(0, 10).scale(100)
                .binding(-params.weightAvoid, v -> params.weightAvoid = -v).build())
            .add(new JLabeledFloatSlider.Builder().label("Scent").range(0, 10).scale(100)
                .binding(params.weightScore, v -> params.weightScore = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Random").range(0, 1).scale(1000)
                .binding(params.weightRandom, v -> params.weightRandom = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Random Arg").range(0, 10).scale(100)
                .binding(params.argRandom, v -> params.argRandom = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Straight").range(0, 2).scale(1000)
                .binding(params.weightStraight, v -> params.weightStraight = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Height").range(0, 100).scale(100)
                .binding(params.weightHeight, v -> params.weightHeight = v).build())
            .addChangeListener(e -> {
                for (Colony colony : simulation.world().colonies()) {
                    colony.setAntSpawnParameters(primaryColony.antSpawnParameters());
                    applyParametersToAllAnts(colony);
                }
            })
            .build();
    }

    @NotNull
    private JComponent createAntParametersPanel(@NotNull Simulation simulation, @NotNull AntParameters params) {
        return new JOptionGroup.Builder()
            .label("Ant Parameters")
            .add(new JLabeledFloatSlider.Builder().label("Straight Bias").range(0, 10).scale(100)
                .binding(params.straightBias, v -> params.straightBias = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Food Gain").range(0, 10).scale(100)
                .binding(params.foodScentGain, v -> params.foodScentGain = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Food Strength").range(0, 10).scale(100)
                .binding(params.foodScentAddend, v -> params.foodScentAddend = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Food Decay").range(0.9f, 1).scale(2000)
                .binding(params.foodScentDecay, v -> params.foodScentDecay = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Colony Gain").range(0, 10).scale(100)
                .binding(params.colonyScentGain, v -> params.colonyScentGain = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Colony Strength").range(0, 10).scale(100)
                .binding(params.colonyScentAddend, v -> params.colonyScentAddend = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Colony Decay").range(0.9f, 1).scale(2000)
                .binding(params.colonyScentDecay, v -> params.colonyScentDecay = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Avoid Strength").range(0, 10).scale(100)
                .binding(params.avoidScentAdded, v -> params.avoidScentAdded = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Avoid Extra").range(0, 10).scale(100)
                .binding(params.avoidScentFactor, v -> params.avoidScentFactor = v).build())
            .add(new JLabeledSlider.Builder().label("Energy Gain").range(1, 100)
                .binding(params.energyGain / 100, v -> params.energyGain = v * 100).build())
            .add(new JLabeledFloatSlider.Builder().label("Energy Food Factor").range(0, 50).scale(10)
                .binding(params.energyFoodFactor / 100, v -> params.energyFoodFactor = v * 100).build())
            .addChangeListener(e -> {
                for (Colony colony : simulation.world().colonies()) {
                    colony.setAntSpawnParameters(primaryColony.antSpawnParameters());
                    applyParametersToAllAnts(colony);
                }
            })
            .build();
    }

    @NotNull
    private JComponent createColonyParametersPanel(@NotNull Simulation simulation, Colony.@NotNull Parameters params) {
        return new JOptionGroup.Builder()
            .label("Colony Parameters")
            .add(new JLabeledFloatSlider.Builder().label("Spawn Cost").range(1, 100).scale(1)
                .binding(params.antSpawnFoodCost, v -> params.antSpawnFoodCost = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Spawn Threshold").range(0, 200).scale(1)
                .binding(params.antSpawnFoodThreshold, v -> params.antSpawnFoodThreshold = v).build())
            .addChangeListener(e -> {
                for (Colony colony : simulation.world().colonies()) {
                    colony.setParameters(primaryColony.parameters());
                }
            })
            .build();
    }

    @NotNull
    private JComponent createWorldParametersPanel(@NotNull WorldParameters wParams) {
        return new JOptionGroup.Builder()
            .label("World Parameters")
            .add(new JLabeledFloatSlider.Builder().label("Food Decay").range(0.95f, 1).scale(2000)
                .binding(wParams.foodDecay, v -> wParams.foodDecay = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Colony Decay").range(0.95f, 1).scale(2000)
                .binding(wParams.colonyDecay, v -> wParams.colonyDecay = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Avoid Decay").range(0.75f, 1).scale(2000)
                .binding(wParams.avoidDecay, v -> wParams.avoidDecay = v).build())
            .add(new JLabeledSlider.Builder().label("Day Cycle Time").range(100, 10000)
                .binding(wParams.dayNightCycleTime, v -> wParams.dayNightCycleTime = v).build())
            .add(new JLabeledFloatSlider.Builder().label("Day Percentage").range(0, 1).scale(1000)
                .binding(wParams.dayPercentage, v -> wParams.dayPercentage = v).build())
            .build();
    }

    @NotNull
    private JButton createResetButton(@NotNull Simulation simulation) {
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            simulation.resetWorld();

            // Carry over parameters to new world
            Colony newPrimaryColony = simulation.world().colonies().get(0);
            newPrimaryColony.setAntSpawnParameters(primaryColony.antSpawnParameters());
            newPrimaryColony.setParameters(primaryColony.parameters());
            primaryColony = newPrimaryColony;

            // Copy parameters to all colonies
            for (Colony colony : simulation.world().colonies()) {
                colony.setAntSpawnParameters(primaryColony.antSpawnParameters());
                colony.setParameters(primaryColony.parameters());
                applyParametersToAllAnts(colony);
            }
        });
        return resetButton;
    }

    /**
     * Adds a vertical separator to the specified parent
     */
    private static void addVerticalSeparator(@NotNull JComponent parent) {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setMaximumSize(new Dimension(5, 0));
        parent.add(separator);
        separator = new JSeparator(JSeparator.VERTICAL);
        separator.setMaximumSize(new Dimension(5, 15));
        parent.add(separator);
    }

    /**
     * Copies the colony ant spawn parameters to all ants of that colony
     */
    private void applyParametersToAllAnts(@NotNull Colony colony) {
        for (Ant ant : colony.ants()) {
            ant.setParameters(new AntParameters(colony.antSpawnParameters()));
        }
    }


    /**
     * Runs the simulation with the specified delay and optionally asynchronously
     */
    public void run(double delaySeconds, boolean async) {
        setDelay(delaySeconds);

        if (async) {
            new Thread(() -> runSimulationWithDelay(false, false)).start();
            new Timer(16, (event) -> paintSimulation(false)).start();
        } else {
            runSimulationWithDelay(true, true);
        }
    }

    /**
     * Runs the simulation with the specified delay and updates the Viewer.
     */
    private void runSimulationWithDelay(boolean paint, boolean sync) {
        // STYLE: Parallel
        // The visualization (world painting) together with the simulation
        // is processed on different thread then the viewer (swing ui).
        // This method is responsible for ensuring consistent milliseconds per tick (update).
        // If the tick-rate lags behind the visualization (painting) is skipped until the simulation
        // catches up. (Usually the updateSimulation call finishes much quicker than paintSimulation.)
        // If the lag becomes too much it is simply reset and a warning is logged.
        // In this case the user should increase the step delay or run the program with better hardware.
        long lagMs = 0;
        long time = System.currentTimeMillis();
        while (windowOpen.get()) {
            long delayMs = delayMsAtomic.get();
            updateSimulation();

            if (paint) {
                if (lagMs >= delayMs * 10 || (delayMs == 0 && lagMs > 1000)) {
                    System.out.println("Warning: Lagging " + lagMs + "ms (>=10 steps) behind! Skipping!");
                    lagMs = 0;
                } else {
                    paintSimulation(sync);
                }
            }

            if (delayMs == 0) {
                time = System.currentTimeMillis();
                continue;
            }

            long tookMs = System.currentTimeMillis() - time;
            long sleepMs = Math.max(delayMs - tookMs, 0) - lagMs / 2;
            lagMs = Math.max(lagMs + (tookMs - delayMs), 0);

            if (sleepMs > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ignored) {
                }
            }
            time = System.currentTimeMillis();
        }
    }

    /**
     * Paints the simulation and updates the score label.
     */
    private void paintSimulation(boolean sync) {
        float food = simulation.world().totalColonyFood();
        int ants = simulation.world().totalAnts();
        int currentTicks = simulation.world().time();
        scoreLabel.setText(String.format("Ants: %d, Food: %.1f", ants, food));
        long currentTime = System.currentTimeMillis();
        if (currentTime >= lastTpsMeasureTime + 1000) {
            long deltaTime = currentTime - lastTpsMeasureTime;
            int deltaTicks = currentTicks - lastTpsMeasureTicks;
            msPerTick = (float) deltaTime / (deltaTicks);
            lastTpsMeasureTime = currentTime;
            lastTpsMeasureTicks = currentTicks;
        }
        timeLabel.setText(String.format("Time: %d (%s) (%.1fmspt)",
            simulation.world().time(),
            simulation.world().isDay() ? "day" : "night",
            msPerTick));
        synchronized (visualization) {
            visualization.repaint();
            if (sync) {
                try {
                    visualization.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    /**
     * Updates the simulation by one step.
     */
    private void updateSimulation() {
        int skip = skipStepAtomic.get();
        if (pausedAtomic.get() && skip == 0) return;
        if (skip > 0) skipStepAtomic.compareAndExchange(skip, skip - 1);
        simulation.step();
    }
}
