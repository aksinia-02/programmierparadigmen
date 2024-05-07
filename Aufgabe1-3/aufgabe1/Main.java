package aufgabe1;

import aufgabe1.view.Viewer;
import aufgabe1.view.Visualization;
import aufgabe1.world.SimpleFoodWorldGenerator;
import aufgabe1.world.World;
import aufgabe1.world.WorldParameters;
import aufgabe1.world.entity.AntParameters;
import aufgabe1.world.entity.Colony;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.random.RandomGeneratorFactory;

/**
 * Modularisierungseinheit: Modul
 */
public class Main {
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
        AtomicBoolean firstSim = new AtomicBoolean(true);
        Simulation sim = new Simulation(randoms, s -> {
            if (firstSim.getAndSet(false)) {
                return createStartingWorld(s, worldParams, colonyParameters, antParams);
            } else {
                return createRandomWorld(s, worldParams, colonyParameters, antParams);
            }
        });

        Visualization.Parameters vizParams = new Visualization.Parameters();
        vizParams.scentColorScale = behaviorParams.highScentThreshold;
        vizParams.foodColorScale = 0.25f;
        vizParams.scale = 4;

        Viewer viewer = new Viewer(sim, vizParams, 1000, 1000);
        viewer.run(1.0 / 25.0, true);
    }

    @NotNull
    private static World createStartingWorld(@NotNull Simulation sim, @NotNull WorldParameters worldParams, Colony.@NotNull Parameters colonyParameters, @NotNull AntParameters antParameters) {
        World world = new World(new SimpleFoodWorldGenerator(1337, sim.randoms(), worldParams), worldParams);
        world.setSimulation(sim);

        Colony colony = world.generator().createColony(world.get(170, 120), colonyParameters, antParameters);
        colony.increaseFood(100);
        Colony colony1 = world.generator().createColony(world.get(80, 80), colonyParameters, antParameters);
        colony1.increaseFood(100);
        for (int i = 0; i < 50; i++) {
            colony.spawnAnt(5, 5);
            colony1.spawnAnt(5, 5);
        }

        return world;
    }

    @NotNull
    private static World createRandomWorld(@NotNull Simulation sim, @NotNull WorldParameters worldParams, Colony.@NotNull Parameters colonyParameters, @NotNull AntParameters antParameters) {
        World world = new World(new SimpleFoodWorldGenerator(1337, sim.randoms(), worldParams), worldParams);
        world.setSimulation(sim);
        for (int i = 0; i < 2; i++) {
            int colonyX = sim.randoms().places().nextInt(250) - 125;
            int colonyY = sim.randoms().places().nextInt(250) - 125;
            Colony colony = world.generator().createColony(world.get(colonyX, colonyY), colonyParameters, antParameters);
            colony.increaseFood(100);
            for (int ant = 0; ant < 50; ant++) {
                colony.spawnAnt(5, 5);
            }
        }

        return world;
    }

}
