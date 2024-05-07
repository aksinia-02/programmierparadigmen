package aufgabe6;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Meta(author = Meta.Author.PRIVAS)
public class Formicarium {
    private final String name;
    private String antName;

    @Invariant("numbers of nests are unique")
    private final LinkedList nests = new LinkedList();

    @Contract(pre = "name is unique", post = "set name of formicarium")
    @Meta(author = Meta.Author.VOROBEVA)
    public Formicarium(String name) {
        this.name = name;
    }

    @Contract(post = "returns amount of nests in formicrium")
    public int nestCount() {
        return nests.size();
    }

    @Contract(pre = "name is unique", post = "set name of ant")
    @Meta(author = Meta.Author.VOROBEVA)
    public void setAntName(String antName) {
        this.antName = antName;
    }

    @Contract(pre = "nest must not be null", post = "returns true is nest belongs to formicarium, otherwise false")
    @Meta(author = Meta.Author.VOROBEVA)
    public boolean containsNest(Nest nest) {
        return nests.contains(nest);
    }

    @Contract(post = "return name")
    public String name() {
        return name;
    }

    @Contract(post = "set null instead of antName")
    @Meta(author = Meta.Author.VOROBEVA)
    public void deleteAntName() {
        this.antName = null;
    }

    @Contract(post = "return antName")
    @Meta(author = Meta.Author.VOROBEVA)
    public String antName() {
        return antName;
    }

    @Contract(post = "add nest to formicarium if it is not included yet")
    @Meta(author = Meta.Author.VOROBEVA)
    public void addNest(Nest nest) {
        if (!nests.contains(nest)) {
            nests.add(nest);
        }
    }

    @Contract(post = "removes nest with nestNumber from nests if it is included")
    public void removeNest(int nestNumber) {
        nests.remove(nestNumber, (number, nest) -> ((Nest) nest).number() == (int) number);
    }

    @Contract(pre = "nest and material must not be null", post = "set new material in provided nest")
    @Meta(author = Meta.Author.VOROBEVA)
    public void setMaterialForNest(Nest nest, Material material) {
        if (nests.contains(nest)) {
            nest.setMaterial(material);
        }
    }

    @Contract(
        post = "Returns the average volume of the nests\n" +
                "Returns zero if list is empty"
    )
    public double averageVolumeOfNests() {
        return average(o -> true, o -> ((Nest) o).volume());
    }

    private double average(Predicate filter, Function valueFunction) {
        double volumeSum = 0;
        double count = 0;
        for (Object nest : nests) {
            if (filter.test(nest)) {
                volumeSum += (double) valueFunction.apply(nest);
                count++;
            }
        }
        return count > 0 ? volumeSum / count : 0.0;
    }

    @Contract(
        post = "Calculates the average volume of the nests with a heating\n" +
            "Returns zero if list contains no nests with heating"
    )
    public double averageVolumeOfNestsWithHeating() {
        return average(o -> o instanceof NestWithHeating, o -> ((Nest) o).volume());
    }

    @Contract(
        post = "Calculates the average volume of the nests with a humidifier\n" +
            "Returns zero if list contains no nests with humidifier"
    )
    public double averageNestsVolumeWithHumidifier() {
        return average(o -> o instanceof NestWithHumidifier, o -> ((Nest) o).volume());
    }

    @Contract(
        post = "Returns the average power of the nests heating\n" +
            "Returns zero if list contains no nests with heating"
    )
    public double averagePowerHeating() {
        return average(o -> o instanceof NestWithHeating, o -> ((NestWithHeating) o).power());
    }

    @Contract(
        post = "Returns the average volume of the watertank of a nest with humidifier\n" +
            "Returns zero if list contains no nests with humidifier"
    )
    public double averageVolumeHumidifier() {
        return average(o -> o instanceof NestWithHumidifier, o -> ((NestWithHumidifier) o).waterContainerVolume());
    }

    @Contract(
        post = "Returns the average weight of sand-clay of all the nests\n" +
            "Returns zero if list is empty"
    )
    public double averageWeightOfSandClayAllNests() {
        return average(o -> true, o -> {
            if (((Nest) o).material() instanceof SandClay sandClay) {
                return sandClay.weight();
            }
            return 0.0;
        });
    }

    @Contract(
        post = "Returns the average weight of sand-clay of nests with heating\n" +
            "Returns zero if list contains no nests with heating"
    )
    public double averageWeightOfSandClayHeatingNests() {
        return average(o -> o instanceof NestWithHeating, o -> {
            if (((Nest) o).material() instanceof SandClay sandClay) {
                return sandClay.weight();
            }
            return 0.0;
        });
    }

    @Contract(
        post = "Returns the average weight of sand-clay of nests with humidifier\n" +
            "Returns zero if list contains no nests with humidifier"
    )
    public double averageWeightOfSandClayHumidifierNests() {
        return average(o -> o instanceof NestWithHumidifier, o -> {
            if (((Nest) o).material() instanceof SandClay sandClay) {
                return sandClay.weight();
            }
            return 0.0;
        });
    }

    @Contract(
        post = "Returns the average volume of aerated-concrete-slab of all nests\n" +
            "Returns zero if list is empty"
    )
    public double averageVolumeOfAeratedConcreteSlabAllNests() {
        return average(o -> true, o -> {
            if (((Nest) o).material() instanceof AeratedConcreteSlab aeratedConcreteSlab) {
                return aeratedConcreteSlab.volume();
            }
            return 0.0;
        });
    }

    @Contract(
        post = "Returns the average volume of aerated-concrete-slab of nests with heating\n" +
            "Returns zero if list contains no nests with heating"
    )
    public double averageVolumeOfAeratedConcreteSlabHeatingNests() {
        return average(o -> o instanceof NestWithHeating, o -> {
            if (((Nest) o).material() instanceof AeratedConcreteSlab aeratedConcreteSlab) {
                return aeratedConcreteSlab.volume();
            }
            return 0.0;
        });
    }

    @Contract(
        post = "Returns the average volume of aerated-concrete-slab of nests with humidifier\n" +
            "Returns zero if list contains no nests with humidifier"
    )
    public double averageVolumeOfAeratedConcreteSlabHumidifierNests() {
        return average(o -> o instanceof NestWithHumidifier, o -> {
            if (((Nest) o).material() instanceof AeratedConcreteSlab aeratedConcreteSlab) {
                return aeratedConcreteSlab.volume();
            }
            return 0.0;
        });
    }


    @Override
    @Meta(author = Meta.Author.VOROBEVA)
    public String toString() {
        StringBuilder result = new StringBuilder("Name of formicarium: " + name + ", name of ants: " + antName + ", contains nests: \n");
        for (Object nest : nests) {
            result.append("\t\t").append(nest.toString()).append("\n");
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Formicarium that = (Formicarium) o;
        return Objects.equals(name, that.name);
    }
}