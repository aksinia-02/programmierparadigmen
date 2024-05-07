import aufgabe4.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// Aufgabenaufteilung:
// Sebastian Privas: Besprechung der Angabe und möglichen Untertypbeziehungen, Erst-Implementierung der Klassen und Iteratoren, Erklärung der nicht vorhandenen Untertypbeziehungen
// Wendelin Muth: Besprechung der Angabe und möglichen Untertypbeziehungen, Erstellung des Klassendiagramms, Schreiben der Tests, Überarbeitung des FormicariumSets
// Aksinia Vorobeva: Besprechung der Angabe und möglichen Untertypbeziehungen, Hinzufügen fehlender Bestandteile in den Klassen, Hinzufügen von Tests

// Begründung für fehlende Untertypbeziehungen:

// Arena:
// Warum Arena kein Foricarium ist: In der Angabe steht: Eine Arena alleine (ohne Nest) bietet Ameisenkolonien keine ausreichende Grundlage für ein
// längeres Überleben, somit kann eine Arena alleine kein Foricarium sein. Wenn Arena ein Foricarium sein könnte, würde diese Zusicherung verletzt sein.
// Warum Arena kein Instrument ist: Instrumente sind Messgeräte oder Werkzeuge, das stellt die Arena in keinster Weise dar, denn Arena ist ein wichtiger
// Bestandteil eines Foricariums.

// Compatibility:
// Ein Compatibility Objekt darf laut Angabe niemals ein Objekt der realen Welt sein und daher darf es auch kein Untertyp von ForicariumItem, ForicariumPart,
// Foricarium oder Instrument sein, da Untertypen dieser Interfaces/Klassen immer physische Objekte der realen Welt darstellen können. Daraus folgt natürlich auch,
// das ein Compatibility Objekt niemals Untertyp eines phyischen Objekts sein kann.

// Forceps
// Warum Forceps kein FormicariumPart ist: In der Angabe steht: "Eine Pinzette wird häufig mit Foricarien verwendet, ist aber kein Bestandteil eines Foricariums"
// und gilt das Forceps kein ForicariumPart ist. Das heißt beispielsweise kann niemals eine Forceps in einer CompositeFormicarium vorkommen und
// folgernd auch selbst kein Foricarium sein.

// Formicarium:
// Warum Foricarium kein Untertyp von Instrument ist: Ein Foricarium ist eine Terrarium, ein Instrument ist ein Werkzeug oder ein Messgerät.
// Ein Foricarium kann aus Bestandteilen wie Arena und Nest bestehen und somit nicht immer aus Instrumenten, daher kann hier keine Untertypbeziehung existieren.

// FormicariumItem:
// Warum FormicariumItem kein Untertyp von FormicariumPart ist: Ein ForicariumItem besteht aus Instrumenten oder ForicariumParts, allerdings muss
// nicht jedes ForicariumPart ein Instrument sein und daher kann hier keine Untertypbeziehung existieren.

// FormicariumPart:
// Warum ein ForicariumPart kein Untertyp von Instrument ist: Ein Instrument ist ein Messgerät oder Werkzeug, allerdings gibt es auch Bestandteile eines Foricariums,
// die das nicht sind, wie z.B. Arena oder Nest und daher kann hier keine Untertypbeziehung existieren.

// Instrument:
// Warum Instrument kein Untertyp von ForicariumItem ist: Nicht jedes Instrument kann zusammen mit Foricarien verwendet werden.
// Warum das Interface Instrument kein Untertyp von ForicariumPart ist: Ein ForicariumPart ist ein Bestandteil eines Foricariums oder ein Foricarium selbst.
// Ein Instrument kann ein Bestandteil eines Foricariums sein, muss es aber nicht, daher ist hier keine Untertypbeziehung möglich, da es auch Instrumente geben
// kann, die kein Bestandteil eines Foricariums sind.

// Nest:
// Warum Nest kein Untertyp von Instrument ist: Instrumente sind Messgeräte oder Werkzeuge, das stellt ein Nest in keinster Weise dar, denn Nest ist ein wichtiger
// Bestandteil eines Foricariums und beherbergt die Ameisenkönigen, die eine Ameisenkolonie erst möglich macht.

// Thermometer:
// Warum Thermometer kein Foricarium ist: Thermometer ist ein Instrument, Instrumente sind Messgeräte und ein Foricarium ist ein Terrarium, somit kann
// ein Thermometer kein Formicarium sein.


public class Test {

    public static void main(String[] args) {
        run("Compatibility should be intersect-able between all types of FormicariumItem", Test::testFormicariumItemCompatabilitySubstitution);
        run("Instrument does not have to be a FormicariumItem", Test::testInstrumentNotFormicariumItem);
        run("ExpandableFormicarium should accept any type of Formicarium as a part", Test::testFormicariumPartOfFormicarium);
        run("Arena is not a Formicarium", Test::testArenaNotFormicarium);
        run("ExpandableFormicarium can be modified", Test::testExpandableFormicarium);
        run("Nest with iterator", Test::testNest);
        run("CompositeFormicarium With One Element", Test::testCompositeFormicariumWithOneElement);
        run("CompositeFormicarium With Two equals Elements", Test::testCompositeFormicariumWithTwoEqualsElements);
        run("CompositeFormicarium With Two NonEquals Elements", Test::testCompositeFormicariumWithTwoNonEqualsElements);
        run("CompositeFormicarium With Two NonEquals Elements RemoveSecond", Test::testCompositeFormicariumWithTwoNonEqualsElementsRemoveSecond);
        run("CompositeFormicarium with two NonEquals elements remove third", Test::testCompositeFormicariumWithThreeNonEqualsElementsRemoveThird);
        run("FormicariumSet With One Element", Test::testFormicariumSetWithOneElement);
        run("FormicariumSet With Two equals Elements", Test::testFormicariumSetWithTwoEqualsElements);
        run("FormicariumSet With Two NonEquals Elements", Test::testFormicariumSetWithTwoNonEqualsElements);
        run("FormicariumSet With Two NonEquals Elements RemoveSecond", Test::testFormicariumSetWithTwoNonEqualsElementsRemoveSecond);
        run("FormicariumSet with two NonEquals elements remove third", Test::testFormicariumSetWithThreeNonEqualsElementsRemoveThird);
        run("Formicarium set with three Elements, two equals, remove one of equals elements", Test::testFormicariumSetWithThreeElementsTwoEquals);
        run("FormicariumSet remove() conditions", Test::testFormicariumSetRemove);
        run("CompositeFormicarium remove() conditions", Test::testCompositeFormicariumRemove);
        run("CompositeFormicarium add equality test", Test::testCompositeFormicariumAddEqualityChange);
        run("CompositeFormicarium remove equality test", Test::testCompositeFormicariumRemoveEqualityChange);

    }

    private static void testExpandableFormicarium() {
        ExpandableFormicarium expandableFormicarium = new CompositeFormicarium();
        expandableFormicarium.add(new Arena(new Compatability(), Substrate.SAND, Material.GLASS));
        checkIteratorSize("Has one part", expandableFormicarium, 1);
        Iterator<FormicariumPart> iterator = expandableFormicarium.iterator();
        iterator.next();
        iterator.remove();
        checkIteratorSize("Has no parts", expandableFormicarium, 0);

        expandableFormicarium.add(new Arena(new Compatability(), Substrate.SAND, Material.GLASS));
        //noinspection UnnecessaryLocalVariable
        Formicarium formicarium = (Formicarium) expandableFormicarium;
        checkIteratorSize("Has one part", formicarium, 1);
        iterator = formicarium.iterator();
        iterator.next();
        iterator.remove();
        checkIteratorSize("Has no parts", formicarium, 0);
    }

    private static void testArenaNotFormicarium() {
        checkException("Arena can't be cast to Formicarium", ClassCastException.class, () -> {
            FormicariumItem item = new Arena(new Compatability(), Substrate.DIRT, Material.GLASS);
            //noinspection DataFlowIssue
            Formicarium _unused = (Formicarium) item;
        });
    }

    private static void testFormicariumPartOfFormicarium() {
        ExpandableFormicarium composite = new CompositeFormicarium();
        Formicarium nest = new Nest(new Compatability());
        composite.add(nest);
    }

    private static void testInstrumentNotFormicariumItem() {
        Instrument instrument = new Instrument() {
            @Override
            public @NotNull InstrumentQuality quality() {
                return InstrumentQuality.CASUAL;
            }
        };


        checkException("Instrument can't be cast to FormicariumItem", ClassCastException.class, () -> {
            @SuppressWarnings("DataFlowIssue")
            FormicariumItem _unused = (FormicariumItem) instrument;
        });
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

    public static void testFormicariumItemCompatabilitySubstitution() {
        List<FormicariumItem> items = List.of(
                new Forceps(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, InstrumentQuality.PROFESSIONAL),
                new Thermometer(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, InstrumentQuality.PROFESSIONAL),
                new Arena(new Compatability(), Substrate.SAND, Material.GLASS),
                new Nest(new Compatability()),
                new AntFarm(new Compatability(), Substrate.SAND, Material.GLASS, Float.POSITIVE_INFINITY),
                new CompositeFormicarium()
        );

        for (FormicariumItem itemA : items) {
            for (FormicariumItem itemB : items) {
                try {
                    Compatability result = intersectFormicariumItemCompatability(itemA, itemB);
                    //noinspection ConstantValue
                    check("result should not be null", result != null);
                } catch (IncompatibleException e) {
                    throw new AssertionError(String.format("%s and %s should be compatible", itemA, itemB), e);
                }
            }
        }
    }

    private static void check(String label, boolean result) {
        if (!result) {
            throw new AssertionError("Check failed: " + label);
        }
    }

    private static void checkIteratorSize(String label, Iterable<?> iterable, int expected) {
        checkIteratorSize(label, iterable.iterator(), expected);
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

    private static void checkList(String label, Iterator<?> iterator, List<?> expected) {
        Iterator iterator1 = expected.iterator();
        while (iterator.hasNext() && iterator1.hasNext()) {
            if (!iterator.next().equals(iterator1.next()))
                throw new AssertionError("List check failed" + ": " + label);
        }
        if (iterator.hasNext() || iterator1.hasNext())
            throw new AssertionError("List check failed" + ": " + label);
    }

    private static void checkList(String label, Iterable<?> iterable, List<?> expected) {
        checkList(label, iterable.iterator(), expected);
    }

    private static void checkException(String label, Class<? extends Throwable> exception, Runnable func) {
        boolean any = false;
        try {
            func.run();
        } catch (Throwable throwable) {
            any = true;
            if (throwable.getClass() != exception) {
                throw new AssertionError("Exception check failed, wrong execution: " + label, throwable);
            }
        }
        if (!any) throw new AssertionError("Exception check failed, no exception: " + label);
    }

    private static Compatability intersectFormicariumItemCompatability(FormicariumItem a, FormicariumItem b) throws IncompatibleException {
        return Objects.requireNonNull(a.compatability()).compatible(b.compatability());
    }

    public static void testNest() {
        Formicarium nest = new Nest(new Compatability());
        Iterator<FormicariumPart> iterator = nest.iterator();
        check("Nest only has itself as a part", iterator.next() == nest);
    }


    public static void testCompositeFormicariumWithOneElement() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        ExpandableFormicarium compositeFormicarium = new CompositeFormicarium();
        compositeFormicarium.add(thermometer);
        checkIteratorSize("Has one parts", compositeFormicarium, 1);
    }


    public static void testCompositeFormicariumWithTwoEqualsElements() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        ExpandableFormicarium compositeFormicarium = new CompositeFormicarium();
        compositeFormicarium.add(thermometer);
        compositeFormicarium.add(thermometer);
        checkIteratorSize("Has one parts", compositeFormicarium, 1);
    }


    public static void testCompositeFormicariumWithTwoNonEqualsElements() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Arena arena = new Arena(new Compatability(), Substrate.DIRT, Material.GLASS);
        ExpandableFormicarium compositeFormicarium = new CompositeFormicarium();
        compositeFormicarium.add(thermometer);
        compositeFormicarium.add(arena);
        checkIteratorSize("Has two parts", compositeFormicarium, 2);
    }


    public static void testCompositeFormicariumWithTwoNonEqualsElementsRemoveSecond() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Arena arena = new Arena(new Compatability(), Substrate.DIRT, Material.GLASS);
        ExpandableFormicarium compositeFormicarium = new CompositeFormicarium();
        compositeFormicarium.add(thermometer);
        compositeFormicarium.add(arena);
        checkIteratorSize("Has two parts", compositeFormicarium, 2);
        Iterator<FormicariumPart> iterator = compositeFormicarium.iterator();
        iterator.next();
        iterator.next();
        iterator.remove();
        checkIteratorSize("Only one part left", compositeFormicarium, 1);
    }


    public static void testCompositeFormicariumWithThreeNonEqualsElementsRemoveThird() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Arena arena = new Arena(new Compatability(), Substrate.DIRT, Material.GLASS);
        Nest nest = new AntFarm(new Compatability(), Substrate.DIRT, Material.GLASS, 2.0f);
        ExpandableFormicarium compositeFormicarium = new CompositeFormicarium();
        compositeFormicarium.add(thermometer);
        compositeFormicarium.add(arena);
        compositeFormicarium.add(nest);
        checkIteratorSize("Has three parts", compositeFormicarium, 3);
        Iterator<FormicariumPart> iterator = compositeFormicarium.iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.remove();
        checkIteratorSize("Two parts left", compositeFormicarium, 2);
        List<FormicariumPart> items = new ArrayList<>();
        items.add(thermometer);
        items.add(arena);
        checkList("Two parts left", compositeFormicarium, items);
    }

    public static void testFormicariumSetWithOneElement() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        FormicariumSet formicariumSet = new FormicariumSet();
        formicariumSet.add(thermometer);
        checkIteratorSize("Has one part", formicariumSet, 1);
    }

    public static void testFormicariumSetWithTwoEqualsElements() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        FormicariumSet formicariumSet = new FormicariumSet();
        formicariumSet.add(thermometer);
        formicariumSet.add(thermometer);
        checkIteratorSize("Has one part", formicariumSet, 1);
    }

    public static void testFormicariumSetWithTwoNonEqualsElements() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Arena arena = new Arena(new Compatability(), Substrate.DIRT, Material.GLASS);
        FormicariumSet formicariumSet = new FormicariumSet();
        formicariumSet.add(thermometer);
        formicariumSet.add(arena);
        checkIteratorSize("Has two parts", formicariumSet, 2);
    }

    public static void testFormicariumSetWithTwoNonEqualsElementsRemoveSecond() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Arena arena = new Arena(new Compatability(), Substrate.DIRT, Material.GLASS);
        FormicariumSet formicariumSet = new FormicariumSet();
        formicariumSet.add(thermometer);
        formicariumSet.add(arena);
        checkIteratorSize("Has two parts", formicariumSet, 2);
        Iterator<FormicariumItem> iterator = formicariumSet.iterator();
        iterator.next();
        iterator.next();
        iterator.remove();
        checkIteratorSize("Only one part left", formicariumSet, 1);
        List<FormicariumItem> items = new ArrayList<>();
        items.add(thermometer);
        checkList("Only one part left", formicariumSet, items);
    }

    public static void testFormicariumSetWithThreeNonEqualsElementsRemoveThird() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Arena arena = new Arena(new Compatability(), Substrate.DIRT, Material.GLASS);
        Nest nest = new AntFarm(new Compatability(), Substrate.DIRT, Material.GLASS, 2.0f);
        FormicariumSet formicariumSet = new FormicariumSet();
        formicariumSet.add(thermometer);
        formicariumSet.add(arena);
        formicariumSet.add(nest);
        checkIteratorSize("Has two parts", formicariumSet, 3);
        Iterator<FormicariumItem> iterator = formicariumSet.iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.remove();
        checkIteratorSize("Two parts left", formicariumSet, 2);
        List<FormicariumItem> items = new ArrayList<>();
        items.add(thermometer);
        items.add(arena);
        checkList("Two parts left", formicariumSet, items);
    }


    public static void testFormicariumSetWithThreeElementsTwoEquals() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Thermometer thermometer1 = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Arena arena = new Arena(new Compatability(), Substrate.DIRT, Material.GLASS);
        FormicariumSet formicariumSet = new FormicariumSet();
        formicariumSet.add(thermometer);
        formicariumSet.add(thermometer1);
        formicariumSet.add(arena);
        checkIteratorSize("Has two parts", formicariumSet, 2);
        FormicariumSet.SetIterator iterator = formicariumSet.iterator();
        iterator.next();
        checkCount("count of thermometer elements", iterator.count(), 2);
        Iterator<FormicariumItem> iterator1 = formicariumSet.iterator();
        iterator1.next();
        iterator1.remove();
        checkIteratorSize("Two part left", formicariumSet, 2);
        List<FormicariumItem> items = new ArrayList<>();
        items.add(thermometer);
        items.add(arena);
        checkList("Two parts part left", formicariumSet, items);
        iterator = formicariumSet.iterator();
        iterator.next();
        checkCount("count of thermometer elements", iterator.count(), 1);
    }

    public static void checkCount(String label, int count, int expected) {
        if (count != expected)
            throw new AssertionError("List check failed" + ": " + label);
    }

    public static void testFormicariumSetRemove() {
        Thermometer thermometer1 = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        Thermometer thermometer2 = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        FormicariumSet formicariumSet = new FormicariumSet();
        formicariumSet.add(thermometer1);
        formicariumSet.add(thermometer2);
        FormicariumSet.SetIterator iterator = formicariumSet.iterator();
        iterator.next();
        iterator.remove(5);
        checkIteratorSize("Has no part", formicariumSet, 0);
        checkException("Throws no such element", NoSuchElementException.class, () -> iterator.remove(1));
        formicariumSet.add(thermometer2);
        FormicariumSet.SetIterator iterator1 = formicariumSet.iterator();
        checkException("Throws no such element", NoSuchElementException.class, iterator1::remove);
    }

    public static void testCompositeFormicariumRemove() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        ExpandableFormicarium compositeFormicarium = new CompositeFormicarium();
        compositeFormicarium.add(thermometer);
        Iterator<FormicariumPart> iterator = compositeFormicarium.iterator();
        checkException("Throws no such element", NoSuchElementException.class, iterator::remove);
    }

    public static void testCompositeFormicariumAddEqualityChange() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        ExpandableFormicarium f1 = new CompositeFormicarium();
        f1.add(thermometer);
        ExpandableFormicarium f2 = new CompositeFormicarium();
        f2.add(thermometer);
        check("Formicariums are equal", f1.equals(f2));
        Arena arena = new Arena(new Compatability(), Substrate.SAND, Material.GLASS);
        f1.add(arena);
        check("Formicariums are not equal", !f1.equals(f2));
    }

    public static void testCompositeFormicariumRemoveEqualityChange() {
        Thermometer thermometer = new Thermometer(-30, 100, InstrumentQuality.CASUAL);
        ExpandableFormicarium f1 = new CompositeFormicarium();
        f1.add(thermometer);
        ExpandableFormicarium f2 = new CompositeFormicarium();
        f2.add(thermometer);
        check("Formicariums are equal", f1.equals(f2));
        Iterator<FormicariumPart> iterator = f1.iterator();
        iterator.next();
        iterator.remove();
        check("Formicariums are not equal", !f1.equals(f2));
    }
}