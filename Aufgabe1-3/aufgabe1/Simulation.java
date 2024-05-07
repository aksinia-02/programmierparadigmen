package aufgabe1;

import aufgabe1.world.World;
import aufgabe1.world.entity.Colony;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a simulation of the world. It can interact and change the world.
 * STYLE: partially procedural, partially functional, partially object-oriented
 */
// GOOD: The methods clearly defines its purpose and focuses on the function of setting field values.
// GOOD: The use of lambdas and streams promotes a functional style and ensures readability.
public class Simulation implements Closeable {
    @NotNull
    private final Randoms randoms;
    @NotNull
    private final WorldFactory worldFactory;

    @NotNull
    private World world;

    public Simulation(@NotNull Randoms randoms, @NotNull WorldFactory worldFactory) {
        Objects.requireNonNull(randoms);
        Objects.requireNonNull(worldFactory);
        this.randoms = randoms;
        this.worldFactory = worldFactory;
        resetWorld();
        // just to satisfy the not-null constraint
        this.world = world();
    }

    /**
     * Resets the world
     */
    public void resetWorld() {
        setWorld(worldFactory.createWorld(this));
    }

    @NotNull
    public World world() {
        return world;
    }

    /**
     * Sets the world of this simulation. The world must not be bound to a different simulation.
     * The world will be bound to this simulation.
     */
    public void setWorld(@NotNull World world) {
        // IDEA is wrong here
        //noinspection ConstantValue
        if (this.world != null) {
            this.close();
        }
        Objects.requireNonNull(world);
        if (world.simulation().isEmpty()) {
            world.setSimulation(this);
        } else if (world.simulation().get() != this) {
            throw new IllegalArgumentException("World is already bound to a different simulation");
        }
        this.world = world;
    }

    @Override
    public void close() {
        world.close();
    }

    /**
     * Advances the simulation by one time step.
     */
    public void step() {
        world.update();
    }

    @NotNull
    public Randoms randoms() {
        return randoms;
    }

    public void saveWorldData() {
        String worldIniFilePath = "Aufgabe1-3/aufgabe1/database/world" + world.uuid() + ".ini";

        try {
            String simulationSection = "World";
            IniFileParser.set(simulationSection, "food", Float.toString(world.totalColonyFood()));
            IniFileParser.set(simulationSection, "time", Integer.toString(world.time()));

            setFieldsToIni(world.parameters().getClass().getFields(), simulationSection, world.parameters());
            int id = 1;
            for (Colony colony : world.colonies()) {
                String colonySection = "Colony" + id;
                IniFileParser.set(colonySection, "food", Float.toString(colony.food()));
                setFieldsToIni(colony.antSpawnParameters().getClass().getFields(), colonySection, colony.antSpawnParameters());
                id++;
            }
            IniFileParser.save(worldIniFilePath);
        } catch (IllegalAccessException | IOException e) {
            throw new RuntimeException("Error while saving world data to INI file: " + e.getMessage(), e);
        }
    }

    private void setFieldsToIni(Field @NotNull [] fields, String section, Object object) throws IllegalAccessException {
        for (Field field : fields) {
            Object value = field.get(object);
            IniFileParser.set(section, field.getName(), value.toString());
        }
    }

    /**
     * STYLE: partially procedural, partially functional
     * The file chooser acts as the procedural part and setting the parameters acts as the functional.
     * This provides a clean differentiation between these two paradigms.
     */
    // BAD: The method could benefit from more explicit handling of potential errors during the loading process.
    public void loadWorldDataFunctional() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("Aufgabe1-3/aufgabe1/database"));

        int result = jFileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = jFileChooser.getSelectedFile();

        try {
            IniFileParser.load(selectedFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Error while loading world data from INI file: " + e.getMessage(), e);
        }
        BiConsumer<String, Object> setSectionData = this::setFieldValues;

        Colony colony = world.colonies().get(0);
        setSectionData.accept("World", world.parameters());
        setSectionData.accept("Colony1", colony.antSpawnParameters());
        System.out.println();
    }

    /**
     * STYLE: functional
     * The use of lambdas, streams and minimum use of side effects and how to handle them guarantees the referential transparency.
     * This method is only focused on its function and sets the fields from the ini file.
     */
    private void setFieldValues(String section, @NotNull Object object) {
        Stream.of(object.getClass().getFields())
            .map(field -> {
                String fieldName = field.getName();
                String fieldValue = IniFileParser.get(section, fieldName);

                if (fieldValue != null) {
                    return new AbstractMap.SimpleEntry<>(field, fieldValue);
                } else {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .forEach(entry -> {
                try {
                    Field field = entry.getKey();
                    String fieldValue = entry.getValue();

                    if (field.getType().isAssignableFrom(int.class)) {
                        field.setInt(object, Integer.parseInt(fieldValue));
                    } else if (field.getType().isAssignableFrom(float.class)) {
                        field.setFloat(object, Float.parseFloat(fieldValue));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error getting/setting field value: " + e.getMessage(), e);
                }
            });
    }

    /**
     * STYLE: functional
     * The clear steps of processing and saving of simulation data guarantees referential transparency.
     * The method explicitly exposes the connection between the data structures used,
     * such as the map-based SimulationFields structure and the INI file, promoting traceability and understandability of the code.
     */
    public void saveWorldDataFunctional() {
        String worldIniFilePath = "Aufgabe1-3/aufgabe1/database/world" + world.uuid() + ".ini";
        Map<String, Map<String, String>> simulationFields = new HashMap<>();
        try {
            String simulationSection = "World";
            simulationFields.compute(simulationSection, (key, value) ->
                Stream.concat(
                    Stream.of(new AbstractMap.SimpleEntry<>("food", Float.toString(world.totalColonyFood()))),
                    Stream.of(new AbstractMap.SimpleEntry<>("time", Integer.toString(world.time())))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            Map<String, String> worldFields = simulationFields.get(simulationSection);
            worldFields.putAll(setFieldsToIniFunctional(world.parameters()));

            simulationFields.putAll(IntStream.range(0, world.colonies().size())
                .mapToObj(id -> {
                    Colony colony = world.colonies().get(id);
                    String colonySection = "Colony" + (id + 1);
                    return new AbstractMap.SimpleEntry<>(colonySection, setFieldsToIniFunctional(colony.antSpawnParameters()));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            IniFileParser.setSections(simulationFields);
            IniFileParser.save(worldIniFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving world data to INI file: " + e.getMessage(), e);
        }
    }

    /**
     * STYLE: functional
     * This method ensures referential transparency by explicitly detailing the extraction process of
     * field values, associating them with their respective names, and transparently handling any
     * potential errors that may occur during the process.
     */
    @NotNull
    private Map<String, String> setFieldsToIniFunctional(@NotNull Object object) {
        return Stream.of(object.getClass().getFields())
            .collect(Collectors.toMap(Field::getName, field -> {
                try {
                    return field.get(object).toString();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error getting field value: " + e.getMessage(), e);
                }
            }));
    }

    /**
     * Modularisierungseinheit: Klasse
     * STYLE: functional
     * used in the Main.java when creating a world or restarting it
     */
    @FunctionalInterface
    public interface WorldFactory {
        @NotNull
        World createWorld(@NotNull Simulation simulation);
    }
}
