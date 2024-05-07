import aufgabe1.Randoms;
import aufgabe1.Simulation;
import aufgabe1.world.SimpleFoodWorldGenerator;
import aufgabe1.world.World;
import aufgabe1.world.WorldParameters;
import aufgabe1.world.entity.AntParameters;
import aufgabe1.world.entity.Colony;
import org.jetbrains.annotations.NotNull;

import java.util.random.RandomGeneratorFactory;

/**
 * Gruppenaufteilung: Aufgabe 1
 * Aksinia: Erstellung der ersten Version
 * Wendelin: Überarbeitung und vervollständigen der ersten Version
 * Sebastian: Refactoring der Endversion und Hinzufügen von Methodenkommentaren
 * <br>
 * Zusammenfassung: Unendliche Welt (Unterteilung in Chunks), 3D Welt (Höhe),
 * Datenbank (Ini File), Endliches Futter, Mehrere Staaten
 * Gruppenaufteilung: Aufgabe 2
 * Aksinia: Mehrere Kolonien, Endliches Futter
 * Wendelin: Unendliche Welt, Tag und Nacht Zyklus ,Multithreading
 * Sebastian: Datenbank (Ini File), 3D Welt
 */
public class Test {
    public static void main(String[] args) {
        WorldParameters worldParams = new WorldParameters();
        worldParams.foodDecay = 0.995f;
        worldParams.colonyDecay = 0.9975f;
        worldParams.avoidDecay = 0.85f;
        worldParams.dayNightCycleTime = 2000;
        worldParams.dayPercentage = 0.6f;
        worldParams.foodExpireTimeMean = (int) (worldParams.dayNightCycleTime * worldParams.dayPercentage);
        worldParams.foodExpireTimeVariance = worldParams.dayNightCycleTime / 2;
        worldParams.foodExpireTimeMin = 1000;

        Colony.Parameters colonyParameters = new Colony.Parameters();
        colonyParameters.antSpawnFoodThreshold = 75;
        colonyParameters.antSpawnFoodCost = 25;

        AntParameters antParams = new AntParameters();
        AntParameters.Behavior behaviorParams = antParams.behavior();

        behaviorParams.highScentThreshold = 1f;
        behaviorParams.badScentFollowTimeThreshold = 50;

        behaviorParams.weightPursue = 2f;
        behaviorParams.weightAvoid = -0.5f;
        behaviorParams.biasMix = 0f;

        behaviorParams.weightScore = 5f;
        behaviorParams.weightRandom = 0.5f;
        behaviorParams.argRandom = 7f;
        behaviorParams.weightStraight = 0.5f;
        behaviorParams.biasChoose = 0f;
        behaviorParams.weightHeight = 20f;

        antParams.carryingCapacity = 1f;
        antParams.straightBias = 1.0f;

        antParams.colonyScentGain = 5f;
        antParams.colonyScentAddend = 2f;
        antParams.colonyScentDecay = 0.9925f;

        antParams.foodScentGain = 2f;
        antParams.foodScentAddend = 2f;
        antParams.foodScentDecay = 0.99f;

        antParams.avoidScentAdded = 0.5f;
        antParams.avoidScentFactor = 2.5f;

        antParams.energyGain = worldParams.dayNightCycleTime * 2;
        antParams.energyFoodFactor = worldParams.dayNightCycleTime / 3f;

        Randoms randoms = new Randoms(RandomGeneratorFactory.of("L32X64MixRandom"), 1337);
        Simulation sim = new Simulation(randoms, s -> createTestWorld(s, worldParams, colonyParameters, antParams));

        for (int run = 0; run < 5; run++) {
            for (int i = 0; i < 1000; i++) {
                sim.step();
            }
            Colony colony = sim.world().colonies().iterator().next();
            if (colony.food() > 0) {
                System.out.println("Successfully collected: " + colony.food() + " food on the " + (run+1) + " run.");
            } else {
                throw new AssertionError("No food collected in 1000 steps");
            }
            sim.resetWorld();
        }
        sim.close();
    }

    @NotNull
    private static World createTestWorld(@NotNull Simulation sim, @NotNull WorldParameters worldParams, Colony.@NotNull Parameters colonyParameters, @NotNull AntParameters antParameters) {
        World world = new World(new SimpleFoodWorldGenerator(1337, sim.randoms(), worldParams), worldParams);
        world.setSimulation(sim);

        Colony colony = world.generator().createColony(world.get(-50, 0), colonyParameters, antParameters);
        for (int i = 0; i < 50; i++) {
            colony.spawnAnt(5, 5);
        }

        return world;
    }
}