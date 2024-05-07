import aufgabe5.*;

import java.lang.invoke.MutableCallSite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/*
 * Aufgabenaufteilung:
 * Sebastian Privas: Implementierung von fehlenden Klassen und Interfaces, Schreiben der Tests gemäß Aufgabenstellung
 * Wendelin Muth: Implementierung des großteils der Klassen und Interfaces, Schreiben von Zusicherungen und Kommentare
 * Aksinia Vorobeva: Implementierung von fehlenden Methoden in CompatibilitySet, StatSet und AbstractRatedSet, Schreiben von Kommentaren, Hinzufügen von fehlenden Tests
 */
public class Test {
    public static void main(String[] args) {
        StatSet<Numeric, Numeric, Numeric> statSetNumNumNum = new StatSet<>();
        fillSetValues(statSetNumNumNum, numericsList());
        fillSetCriteria(statSetNumNumNum, numericsList());

        StatSet<Part, Part, Quality> statSetPPQ = new StatSet<>();
        fillSetValues(statSetPPQ, partsList());
        fillSetCriteria(statSetPPQ, partsList());

        StatSet<Arena, Part, Quality> statSetAPQ = new StatSet<>();
        fillSetValues(statSetAPQ, arenaList());
        fillSetCriteria(statSetAPQ, partsList());

        StatSet<Nest, Part, Quality> statSetNPQ = new StatSet<>();
        StatSet<Part, Arena, Quality> statSetPAQ = new StatSet<>();
        fillSetValues(statSetPAQ, partsList());
        fillSetCriteria(statSetPAQ, arenaList());

        StatSet<Arena, Arena, Quality> statSetAAQ = new StatSet<>();
        fillSetValues(statSetAAQ, arenaList());
        fillSetCriteria(statSetAAQ, arenaList());

        StatSet<Nest, Arena, Quality> statSetNAQ = new StatSet<>();
        fillSetValues(statSetNAQ, nestList());
        fillSetCriteria(statSetNAQ, arenaList());

        StatSet<Part, Nest, Quality> statSetPNQ = new StatSet<>();
        StatSet<Arena, Nest, Quality> statSetANQ = new StatSet<>();
        StatSet<Nest, Nest, Quality> statSetNNQ = new StatSet<>();
        fillSetValues(statSetNNQ, nestList());
        fillSetCriteria(statSetNNQ, nestList());

        CompatibilitySet<Numeric, Numeric> compatibilitySetNumNum = new CompatibilitySet<>();
        fillSetCriteria(compatibilitySetNumNum, numericsList());
        fillSetCriteria(compatibilitySetNumNum, numericsList());

        CompatibilitySet<Part, Quality> compatibilitySetPQ = new CompatibilitySet<>();
        fillSetValues(compatibilitySetPQ, partsList());
        fillSetCriteria(compatibilitySetPQ, partsList());

        CompatibilitySet<Arena, Quality> compatibilitySetAQ = new CompatibilitySet<>();
        fillSetValues(compatibilitySetAQ, arenaList());
        fillSetCriteria(compatibilitySetAQ, arenaList());

        CompatibilitySet<Nest, Quality> compatibilitySetNQ = new CompatibilitySet<>();
        fillSetValues(compatibilitySetNQ, nestList());
        fillSetCriteria(compatibilitySetNQ, nestList());

        statSetANQ.add(new Arena(Quality.SEMIPROFESSIONAL, 100f));
        statSetANQ.add(new Arena(Quality.HOBBY, 150f));
        statSetANQ.add(new Arena(Quality.PROFESSIONAL, 120f));
        statSetANQ.addCriterion(new Nest(Quality.PROFESSIONAL, 4f));
        statSetANQ.addCriterion(new Nest(Quality.SEMIPROFESSIONAL, 1f));
        statSetANQ.addCriterion(new Nest(Quality.PROFESSIONAL, 2f));
        for (Arena arena : statSetANQ) {
            arena.volume();
            statSetPNQ.add(arena);
            statSetNPQ.addCriterion(arena);
        }
        Iterator<Nest> criterionIterator = statSetANQ.criterions();
        while (criterionIterator.hasNext()) {
            Nest nest = criterionIterator.next();
            nest.antSize();
            statSetPNQ.add(nest);
            statSetNPQ.add(nest);
        }

        // This test tests the promises (Zusicherungen) from RatedSet to guarantee that CompatibilitySet is a subtype of StatSet
        run("test RatedSet promises for CompatibilitySet", Test::testRatedSetRequirements);

        run("test add function in StatSet, check size", Test::testAddInStatSetNumericNumericNumeric);
        run("test add function in StatSet with two equal elements", Test::testStatSetNumericNumericNumericAddEqualsElements);
        run("test remove in iterator of added elements in StatSet", () -> testSetRemoveAddElement(statSetANQ));
        fillSetValues(statSetANQ, arenaList());
        run("test remove in iterator of added elements in CompatibilitySet", () -> testSetRemoveAddElement(compatibilitySetAQ));
        fillSetValues(compatibilitySetAQ, arenaList());
        run("test iterator with Quality parameter in StatSet", () -> testSetIteratorWithQualityParameter(statSetANQ));
        run("test iterator with Quality parameter in CompatibilitySet", () -> testSetIteratorWithQualityParameter1(compatibilitySetAQ));
        run("test iterator with Quality and Criterion parameters in StatSet", () -> testStatSetANQIteratorWithQualityAndCriterion(statSetANQ));
        run("check for equality of two StatSet", Test::testTwoEqualStatSet);

        run("test Arena rated", Test::testArenaRated);
        run("test Nest rated with two Nests", Test::testNestRatedWithTwoNests);
        run("test Nest rated", Test::testNestRated);
        run("test Compatibility rated", () -> testCompatibilityRated(compatibilitySetAQ));

        run("test identical in CompatibilitySet With Arena", Test::testIdenticalInCompatibilitySet);
        run("test identical in CompatibilitySet With Nest", Test::testIdenticalInCompatibilitySet1);

        run("test Numeric setCriterion Lambda a -> a * 2", () -> testNumericSetCriterion(statSetNumNumNum));
        run("test Numeric setCriterion Lambda a -> a * a", () -> testNumericSetCriterion1(statSetNumNumNum));
        run("test Numeric Rated with parameter Lambda a -> a * 3", () -> testNumericRatedWithParameter(statSetNumNumNum));
        run("test Numeric with two setCriterion Lambda a -> a * 2, a -> a / 3", () -> testNumericWithTwoSetCriterion(statSetNumNumNum));

        run("test iterator with Numeric Parameter in StatSet", ()-> testIteratorInStatSetNumNumNum(statSetNumNumNum));
    }

    public static <X extends Rated<? super P, R>, P, R extends Calc<R>> void fillSetValues(StatSet<X, P, R> set, Collection<X> collection) {
        for (X item : collection) {
            set.add(item);
        }
    }

    public static List<Numeric> numericsList() {
        List<Numeric> numerics = new ArrayList<>();
        numerics.add(new Numeric(3));
        numerics.add(new Numeric(4));
        numerics.add(new Numeric(5));

        return numerics;
    }

    public static <X extends Rated<? super P, R>, P, R extends Calc<R>> void fillSetCriteria(StatSet<X, P, R> set, Collection<P> collection) {
        for (P item : collection) {
            set.addCriterion(item);
        }
    }

    public static List<Part> partsList() {
        List<Part> parts = new ArrayList<>();
        parts.add(new Nest(Quality.PROFESSIONAL, 3));
        parts.add(new Arena(Quality.SEMIPROFESSIONAL, 100));
        parts.add(new Arena(Quality.HOBBY, 200));

        return parts;
    }

    public static List<Arena> arenaList() {
        List<Arena> arenas = new ArrayList<>();
        arenas.add(new Arena(Quality.PROFESSIONAL, 120));
        arenas.add(new Arena(Quality.SEMIPROFESSIONAL, 112));
        arenas.add(new Arena(Quality.HOBBY, 56));
        return arenas;
    }

    public static List<Nest> nestList() {
        List<Nest> nests = new ArrayList<>();
        nests.add(new Nest(Quality.PROFESSIONAL, 4));
        nests.add(new Nest(Quality.SEMIPROFESSIONAL, 2));
        nests.add(new Nest(Quality.UNUSABLE, 3));
        return nests;
    }

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

    private static void testRatedSetRequirements() {
        CompatibilitySet<Numeric, Numeric> compatibilitySet = new CompatibilitySet<>();
        Numeric numeric1 = new Numeric(3);
        compatibilitySet.add(numeric1);
        compatibilitySet.add(numeric1);
        checkIteratorSize("Has 1 element", compatibilitySet.iterator(), 1);
        compatibilitySet.addCriterion(numeric1);
        compatibilitySet.addCriterion(numeric1);
        checkIteratorSize("Has 2 elements", compatibilitySet.criterions(), 2);
        compatibilitySet.add(new Numeric(4));
        compatibilitySet.add(new Numeric(2));
        compatibilitySet.add(new Numeric(5));
        checkIteratorSize("Has 2 elements", compatibilitySet.iterator(new Numeric(4)), 2);
        Numeric numeric2 = new Numeric(4);
        numeric2.setCriterion(a -> 2 * a);
        checkIteratorSize("Has 3 elements", compatibilitySet.iterator(numeric2, new Numeric(3)), 3);
    }

    public static void testAddInStatSetNumericNumericNumeric() {
        StatSet<Numeric, Numeric, Numeric> statSet = new StatSet<>();
        statSet.add(new Numeric(2f));
        statSet.add(new Numeric(3f));
        statSet.add(new Numeric(4f));
        checkIteratorSize("Has 3 elements", statSet.iterator(), 3);
    }

    public static void testStatSetNumericNumericNumericAddEqualsElements() {
        StatSet<Numeric, Numeric, Numeric> statSet = new StatSet<>();
        Numeric numeric = new Numeric(2f);
        statSet.add(numeric);
        statSet.add(numeric);
        checkIteratorSize("Has 1 element", statSet.iterator(), 1);
    }

    public static <X extends Rated<? super P, R>, P, R extends Calc<R>> void testSetRemoveAddElement(StatSet<X, P, R> set) {
        Iterator<X> iterator = set.iterator();
        iterator.next();
        iterator.remove();
        checkIteratorSize("Has 2 elements", set.iterator(), 2);
        iterator.next();
        iterator.remove();
        checkIteratorSize("Has 1 elements", set.iterator(), 1);
        iterator.next();
        iterator.remove();
        checkIteratorSize("Has 0 elements", set.iterator(), 0);
    }

    public static <X extends Rated<? super P, aufgabe5.Quality>, P, Quality> void testSetIteratorWithQualityParameter(StatSet<X, P, aufgabe5.Quality> set) {
        checkIteratorSize("Has 2 elements", set.iterator(aufgabe5.Quality.SEMIPROFESSIONAL), 2);
    }

    public static <X extends Rated<? super P, aufgabe5.Quality>, P, Quality> void testSetIteratorWithQualityParameter1(StatSet<X, P, aufgabe5.Quality> set) {
        checkIteratorSize("Has 0 elements", set.iterator(aufgabe5.Quality.SEMIPROFESSIONAL), 0);
    }

    public static void testStatSetANQIteratorWithQualityAndCriterion(StatSet<Arena, Nest, Quality> statSet) {
        checkIteratorSize("Has 2 element", statSet.iterator(new Nest(Quality.SEMIPROFESSIONAL, 3f), Quality.SEMIPROFESSIONAL), 2);
    }

    public static void testTwoEqualStatSet() {
        StatSet<Numeric, Numeric, Numeric> statSet = new StatSet<>();
        statSet.add(new Numeric(2f));
        statSet.addCriterion(new Numeric(3f));
        statSet.iterator();
        StatSet<Numeric, Numeric, Numeric> statSet1 = new StatSet<>();
        statSet1.addCriterion(new Numeric(5f));
        statSet1.add(new Numeric(4f));
        statSet1.iterator();
        checkBoolean("Check for equality is failed", statSet.equals(statSet1), true);
    }

    private static void testArenaRated() {
        Arena arena = new Arena(Quality.SEMIPROFESSIONAL, 200f);
        Nest nest = new Nest(Quality.PROFESSIONAL, 3f);
        checkBoolean("check for compatibility of two part Objects is failed", arena.rated(nest).equals(Quality.SEMIPROFESSIONAL), true);
    }

    private static void testNestRatedWithTwoNests() {
        Nest nest = new Nest(Quality.PROFESSIONAL, 3f);
        Nest nest1 = new Nest(Quality.HOBBY, 2f);
        checkBoolean("check for non-compatibility of two nest Objects is failed", nest.rated(nest1).equals(Quality.UNUSABLE), true);
    }

    private static void testNestRated() {
        Nest nest = new Nest(Quality.SEMIPROFESSIONAL, 3f);
        Arena arena = new Arena(Quality.HOBBY, 200f);
        checkBoolean("check for compatibility of two part Objects is failed", arena.rated(nest).equals(Quality.HOBBY), true);
    }

    private static void testCompatibilityRated(CompatibilitySet<? extends Part, Quality> compatibilitySet) {
        checkBoolean("check for compatibility of CompatibilitySet Objects is failed", compatibilitySet.rated().equals(Quality.HOBBY), true);
    }

    private static void testIdenticalInCompatibilitySet() {
        CompatibilitySet<Arena, Quality> compatibilitySet = new CompatibilitySet();
        Arena arena = new Arena(Quality.SEMIPROFESSIONAL, 200f);
        compatibilitySet.add(arena);
        compatibilitySet.addCriterion(arena);
        compatibilitySet.add(new Arena(Quality.HOBBY, 300f));
        compatibilitySet.addCriterion(new Arena(Quality.HOBBY, 300f));
        compatibilitySet.add(new Arena(Quality.PROFESSIONAL, 100f));
        checkIteratorSize("Has 2 elements", compatibilitySet.identical(), 2);
    }

    private static void testIdenticalInCompatibilitySet1() {
        CompatibilitySet<Nest, Quality> compatibilitySet = new CompatibilitySet();
        Nest nest = new Nest(Quality.SEMIPROFESSIONAL, 200f);
        compatibilitySet.add(nest);
        compatibilitySet.addCriterion(nest);
        compatibilitySet.add(new Nest(Quality.HOBBY, 300f));
        compatibilitySet.addCriterion(new Nest(Quality.HOBBY, 300f));
        compatibilitySet.add(new Nest(Quality.PROFESSIONAL, 100f));
        checkIteratorSize("Has 2 elements", compatibilitySet.identical(), 2);
    }

    private static void checkIteratorSize(String label, Iterator<?> iterator, int expected) {
        int count = 0;
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        if (count != expected) {
            throw new AssertionError("Iterator size check failed, expected " + expected + " got " + count + ": " + label);
        }
    }

    private static void checkBoolean(String label, boolean value, boolean expected) {
        if (expected != value) {
            throw new AssertionError(label);
        }
    }

    private static void testIteratorInStatSetNumNumNum(StatSet<Numeric, Numeric, Numeric> setNumeric){
        checkIteratorSize("Has 1 elements", setNumeric.iterator(new Numeric(4.5f)), 1);
    }

    private static void testNumericSetCriterion(StatSet<Numeric, Numeric, Numeric> setNumeric){
        Numeric numeric = setNumeric.iterator().next();
        numeric.setCriterion(a -> a * 2);
        Numeric result = numeric.rated();
        checkBoolean("check boolean with setCriterion a -> a * 2 is failed", result.value() == 6f,  true);
    }

    private static void testNumericSetCriterion1(StatSet<Numeric, Numeric, Numeric> setNumeric){
        Numeric numeric = setNumeric.iterator().next();
        numeric.setCriterion(a -> a * a);
        Numeric result = numeric.rated();
        checkBoolean("check for equality is failed", result.value() == 9f,  true);
    }

    private static void testNumericRatedWithParameter(StatSet<Numeric, Numeric,Numeric> setNumeric){
        Numeric numeric = setNumeric.iterator().next();
        Numeric result = numeric.rated(a -> a * 3);
        checkBoolean("check for equality is failed", result.value() == 9f, true);
    }

    private static void testNumericWithTwoSetCriterion(StatSet<Numeric, Numeric, Numeric> set){
        Numeric numeric = set.iterator().next();
        numeric.setCriterion(a -> a * 2);
        numeric.setCriterion(a -> a / 3);
        Numeric result = numeric.rated();
        checkBoolean("check for equality is failed", result.value() == 1f, true);
    }

}
