import aufgabe6.*;


@Meta(author = Meta.Author.VOROBEVA)
public class Test {

    public static void main(String[] args) {
        System.out.println("===== Context A =====\n");

        Institute instituteA = new Institute("A");
        Institute instituteB = new Institute("B");
        Institute instituteC = new Institute("C");
        Institute instituteD = new Institute("D");

        Formicarium formicariumA1 = new Formicarium("A1");
        Formicarium formicariumA2 = new Formicarium("A2");
        Formicarium formicariumB1 = new Formicarium("B1");
        Formicarium formicariumB2 = new Formicarium("B2");
        Formicarium formicariumB3 = new Formicarium("B3");
        Formicarium formicariumC1 = new Formicarium("C1");
        Formicarium formicariumC2 = new Formicarium("C2");

        instituteA.addFormicarium(formicariumA1);
        instituteA.addFormicarium(formicariumA2);

        instituteB.addFormicarium(formicariumB1);
        instituteB.addFormicarium(formicariumB2);
        instituteB.addFormicarium(formicariumB3);

        instituteC.addFormicarium(formicariumC1);
        instituteC.addFormicarium(formicariumC2);

        Nest nest1 = new NestWithHeating(30, 20, new SandClay(3), 20);
        Nest nest2 = new NestWithHumidifier(40, 50, new AeratedConcreteSlab(4, 5), 10);
        Nest nest3 = new NestWithHeating(35, 25, new SandClay(5), 30);
        Nest nest4 = new NestWithHumidifier(30, 50, new AeratedConcreteSlab(2, 5), 10);
        Nest nest5 = new NestWithHeating(20, 20, new SandClay(7), 10);


        formicariumB1.addNest(nest1);
        formicariumB1.addNest(nest2);

        formicariumA1.addNest(nest1);
        formicariumA1.addNest(nest2);
        formicariumA1.addNest(nest3);
        formicariumA1.addNest(nest4);
        formicariumA1.addNest(nest5);

        System.out.println("---tests for toString method---");
        run("\nfor Institute:", () -> testToString(instituteB));
        run("\nfor Formicarium:", () -> testToString(formicariumA1));
        run("\nfor Nest:", () -> testToString(nest1));

        System.out.println("\n\n---tests for averageVolumeOfNests in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageVolumeOfNests(), 0));
        run("\nwith more nests:", () -> checkEquals(formicariumB1.averageVolumeOfNests(), 2600));

        System.out.println("\n\n---tests for averageNestsVolumeWithHumidifier in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageNestsVolumeWithHumidifier(), 0));
        run("\nwith more nests:", () -> checkEquals(formicariumA1.averageNestsVolumeWithHumidifier(), 3500));

        System.out.println("\n\n---tests for averageVolumeOfNestsWithHeating in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageVolumeOfNestsWithHeating(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averageVolumeOfNestsWithHeating(), 1250));

        System.out.println("\n\n---tests for averagePowerHeating in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averagePowerHeating(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averagePowerHeating(), 20));

        System.out.println("\n\n---tests for averageVolumeHumidifier in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageVolumeHumidifier(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averageVolumeHumidifier(), 10));

        System.out.println("\n\n---tests for averageVolumeOfAeratedConcreteSlabAllNests in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageVolumeOfAeratedConcreteSlabAllNests(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averageVolumeOfAeratedConcreteSlabAllNests(), 12));

        System.out.println("\n\n---tests for averageVolumeOfAeratedConcreteSlabHeatingNests in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageVolumeOfAeratedConcreteSlabHeatingNests(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averageVolumeOfAeratedConcreteSlabHeatingNests(), 0));

        System.out.println("\n\n---tests for averageVolumeOfAeratedConcreteSlabHumidifierNests in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageVolumeOfAeratedConcreteSlabHumidifierNests(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averageVolumeOfAeratedConcreteSlabHumidifierNests(), 30));


        System.out.println("\n\n---tests for averageWeightOfSandClayAllNests in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageWeightOfSandClayAllNests(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averageWeightOfSandClayAllNests(), 3));

        System.out.println("\n\n---tests for averageWeightOfSandClayHeatingNests in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageWeightOfSandClayHeatingNests(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averageWeightOfSandClayHeatingNests(), 5));

        System.out.println("\n\n---tests for averageWeightOfSandClayHumidifierNests in Formicarium---");
        run("\nwith 0 nests:", () -> checkEquals(formicariumA2.averageWeightOfSandClayHumidifierNests(), 0));
        run("\nwith more nests", () -> checkEquals(formicariumA1.averageWeightOfSandClayHumidifierNests(), 0));


        Material material = new AeratedConcreteSlab(4, 4);

        System.out.println("\n\n---tests for substitute of nest filling---");
        run("\nin Nest:", () -> testForSubstituteOfNestFillingInNest(nest5, material));
        run("\nin Formicarium", () -> testForSubstitutingOfNestFillingInFormicarium(formicariumA1, nest3, material));

        System.out.println("\n\n---tests for antName---");
        run("\nadd name:", () -> testAddAntName(formicariumA1, "nameOfAnt1"));
        run("\nremove name:", () -> testRemoveAntName(formicariumA1));

        System.out.println("\n\n---tests substitute of nests in formicarium---");
        run("\nadd nest", () -> testAddNest(formicariumA2, nest1));
        run("\nadd existing nest test", () -> testAddExistingNest(formicariumA2, nest1));
        run("\nremove nest", () -> testRemoveNest(formicariumA2, nest1));
        run("\nremove non existing nest test", () -> testRemoveNonExistingNest(formicariumA2));

        System.out.println("\n\n---tests substitute of formicariums in institute---");
        run("\nadd formicarium", () -> testAddFormicarium(instituteD, formicariumA1));
        run("\nadd existing formicarium test", () -> testAddExistingNest(instituteD, formicariumA1));
        Formicarium formicarium = new Formicarium("A1");
        formicarium.setAntName("antName2");
        run("\nadd formicarium with equal name:", () -> testAddFormicariumWithEqualName(formicarium, instituteD));
        run("\nremove formicarium", () -> testRemoveFormicarium(instituteD, formicariumA1));
        run("\nremove non existing formicarium test", () -> testRemoveNonExistingFormicarium(instituteB));

        System.out.println("\n\n---check material characteristics---");
        run("\nNest with Sand-Clay returns weight", () -> testMaterialCharacteristicSandClay(nest1));
        run("\nNest with Aerated-Concrete-Slab returns measurements (width, height", () -> testMaterialCharacteristicAeratedConcreteSlab(nest2));

        System.out.println("\n\n---print formicarium statistics of A1---");
        System.out.println("Average volume of all nests: " + formicariumB1.averageVolumeOfNests());
        System.out.println("Average volume of nests with humidifier: " + formicariumB1.averageNestsVolumeWithHumidifier());
        System.out.println("Average volume of nests with heating: " + formicariumB1.averageVolumeOfNestsWithHeating());
        System.out.println("Average power of the heating: " + formicariumB1.averagePowerHeating());
        System.out.println("Average volume of the watercontainer: " + formicariumB1.averageVolumeHumidifier());
        System.out.println("Average volume of aerated concrete slab for all nests: " + formicariumB1.averageVolumeOfAeratedConcreteSlabAllNests());
        System.out.println("Average volume of aerated concrete slab for nests with heating: "+ formicariumB1.averageVolumeOfAeratedConcreteSlabHeatingNests());
        System.out.println("Average volume of aerated concrete slab for nests with humidifier: " + formicariumB1.averageVolumeOfAeratedConcreteSlabHumidifierNests());
        System.out.println("Average weight of sand clay for all nests: " +formicariumB1.averageWeightOfSandClayAllNests());
        System.out.println("Average weight of sand clay for nests with heating: " + formicariumB1.averageWeightOfSandClayHeatingNests());
        System.out.println("Average weight of sand clay for nests with humidifier: " +formicariumB1.averageWeightOfSandClayHumidifierNests());


        run("\n\n===== Context B =====\n", () -> testDataExtractor());
    }

    @Meta(author = Meta.Author.MUTH)
    private static void run(String label, Runnable test) {
        System.out.println(label);
        try {
            test.run();
        } catch (Throwable e) {
            System.out.println("\tfail");
            if (!(e instanceof AssertionError)) {
                throw new AssertionError("Unexpected exception", e);
            }
            throw e;
        }
        System.out.println("\tsuccess");
    }

    private static void testToString(Object object) {
        System.out.println(object);
    }

    @Meta(author = Meta.Author.MUTH)
    private static void catchException(Class<? extends Throwable> exception, Runnable func) {
        boolean any = false;
        try {
            func.run();
        } catch (Throwable throwable) {
            any = true;
            if (throwable.getClass() != exception) {
                throw new AssertionError("Exception check failed, wrong execution", throwable);
            }
        }
        if (!any) throw new AssertionError("Exception check failed");
    }

    private static void checkEquals(double value, double expected) {
        if (expected != value)
            throw new AssertionError("provided value " + value + " not equal with expected value " + expected);
    }

    private static void testForSubstituteOfNestFillingInNest(Nest nest, Material material) {
        nest.setMaterial(material);
        checkEqualsOfObject(nest.material(), material);
    }

    private static void testForSubstitutingOfNestFillingInFormicarium(Formicarium formicarium, Nest nest, Material material) {
        formicarium.setMaterialForNest(nest, material);
        checkEqualsOfObject(nest.material(), material);
    }

    private static void testAddAntName(Formicarium formicarium, String name) {
        formicarium.setAntName(name);
        checkEqualsOfObject(formicarium.antName(), name);
    }

    private static void testRemoveAntName(Formicarium formicarium) {
        formicarium.deleteAntName();
        if (formicarium.antName() != null)
            throw new AssertionError("provided name is not null");
    }

    private static void testAddNest(Formicarium formicarium, Nest nest) {
        formicarium.addNest(nest);
        if (!formicarium.containsNest(nest))
            throw new AssertionError("nest is not added");
    }

    @Meta(author = Meta.Author.PRIVAS)
    private static void testAddExistingNest(Formicarium formicarium, Nest nest) {
        int size = formicarium.nestCount();
        formicarium.addNest(nest);
        if (size != formicarium.nestCount()) {
            throw new AssertionError("Added nest with already existing nest number");
        }
    }

    private static void testAddFormicariumWithEqualName(Formicarium formicarium, Institute institute) {
        int size = institute.formicariumCount();
        institute.addFormicarium(formicarium);
        if (size != institute.formicariumCount()) {
            throw new AssertionError("Added nest with already existing nest number");
        }
    }

    private static void testRemoveNest(Formicarium formicarium, Nest nest) {
        formicarium.removeNest(nest.number());
        if (formicarium.containsNest(nest))
            throw new AssertionError("nest is not removed");
    }

    @Meta(author = Meta.Author.PRIVAS)
    private static void testRemoveNonExistingNest(Formicarium formicarium) {
        int size = formicarium.nestCount();
        formicarium.removeNest(43294);

        if (size != formicarium.nestCount())
            throw new AssertionError("non existing nest removed");
    }

    private static void testAddFormicarium(Institute institute, Formicarium formicarium) {
        institute.addFormicarium(formicarium);
        if (!institute.containsFormicarium(formicarium))
            throw new AssertionError("nest is not added");
    }

    @Meta(author = Meta.Author.PRIVAS)
    private static void testAddExistingNest(Institute institute, Formicarium formicarium) {
        int size = institute.formicariumCount();
        institute.addFormicarium(formicarium);
        if (size != institute.formicariumCount()) {
            throw new AssertionError("Added formicarium with already existing foricarium name");
        }
    }

    private static void testRemoveFormicarium(Institute institute, Formicarium formicarium) {
        institute.removeFormicarium(formicarium.name());
        if (institute.containsFormicarium(formicarium))
            throw new AssertionError("nest is not removed");
    }

    @Meta(author = Meta.Author.PRIVAS)
    private static void testRemoveNonExistingFormicarium(Institute institute) {
        int size = institute.formicariumCount();
        institute.removeFormicarium("Some Name");

        if (size != institute.formicariumCount())
            throw new AssertionError("non existing formicarium removed");
    }

    @Meta(author = Meta.Author.PRIVAS)
    private static void testMaterialCharacteristicSandClay(Nest nest) {
        if (!(nest.materialCharacteristic() instanceof Double)) {
            throw new AssertionError("returned wrong material characteristic");
        }
    }

    @Meta(author = Meta.Author.PRIVAS)
    private static void testMaterialCharacteristicAeratedConcreteSlab(Nest nest) {
        if (!(nest.materialCharacteristic() instanceof Measurements)) {
            throw new AssertionError("returned wrong material characteristic");
        }
    }

    @Meta(author = Meta.Author.MUTH)
    private static void testDataExtractor() {
        try {
            DataExtractor e = new DataExtractor();
            e.addPackage("aufgabe6");
            e.addPackage("");
            e.run();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkEqualsOfObject(Object provided, Object expected) {
        if (!provided.equals(expected)) {
            throw new AssertionError("provided object not equal with expected object");
        }
    }
}
