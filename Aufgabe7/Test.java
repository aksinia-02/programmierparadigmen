import java.lang.reflect.InvocationTargetException;

/*
 * Aufgabenaufteilung:
 * Sebastian Privas: Hinzufügen von AspectJ, Hinzufügen von Advices für Visitor/Element Methoden, Hinzufügen fehlender Tests, Kommentare
 * Wendelin Muth: Entwicklung der Erstversion (Kontext A) und Überarbeitung der Endversion (Kontext A)
 * Aksinia Vorobeva: Hinzufügen von Tests, Hinzufügen von fehlenden Kommentaren, Überarbeitung der Erstversion (Kontext A)
 */
public class Test {

    public static void main(String[] args) {
        Util.errorHandler = s -> {
            throw new RuntimeException(s);
        };
        Institute i1 = new Institute();
        Institute i2 = new Institute();
        Institute i3 = new Institute();

        Form formLargeRegulated1 = new Form(new FormSizeLarge(), new FormRegulationRegulated(), 10.5f);
        Form formLargeUnregulated1 = new Form(new FormSizeLarge(), new FormRegulationUnregulated(), 8.5f);
        Form formMediumRegulated1 = new Form(new FormSizeMedium(), new FormRegulationRegulated(), 8.25f);
        Form formMediumUnregulated1 = new Form(new FormSizeMedium(), new FormRegulationUnregulated(), 7.5f);
        Form formSmallRegulated1 = new Form(new FormSizeSmall(), new FormRegulationRegulated(), 6.3f);
        Form formSmallUnregulated1 = new Form(new FormSizeSmall(), new FormRegulationUnregulated(), 5.2f);

        Form formLargeRegulated2 = new Form(new FormSizeLarge(), new FormRegulationRegulated(), 9.7f);
        Form formLargeUnregulated2 = new Form(new FormSizeLarge(), new FormRegulationUnregulated(), 8.3f);
        Form formMediumRegulated2 = new Form(new FormSizeMedium(), new FormRegulationRegulated(), 7.95f);
        Form formMediumUnregulated2 = new Form(new FormSizeMedium(), new FormRegulationUnregulated(), 6.3f);
        Form formSmallRegulated2 = new Form(new FormSizeSmall(), new FormRegulationRegulated(), 7.2f);
        Form formSmallUnregulated2 = new Form(new FormSizeSmall(), new FormRegulationUnregulated(), 5.0f);

        Colony colonyEuropeLarge1 = new Colony(new ColonyOriginEurope(), new ColonySizeLarge());
        Colony colonyEuropeMedium1 = new Colony(new ColonyOriginEurope(), new ColonySizeMedium());
        Colony colonyEuropeSmall1 = new Colony(new ColonyOriginEurope(), new ColonySizeSmall());
        Colony colonyEuropeLarge2 = new Colony(new ColonyOriginEurope(), new ColonySizeLarge());
        Colony colonyEuropeMedium2 = new Colony(new ColonyOriginEurope(), new ColonySizeMedium());
        Colony colonyEuropeSmall2 = new Colony(new ColonyOriginEurope(), new ColonySizeSmall());
        Colony colonyEuropeSmall3 = new Colony(new ColonyOriginEurope(), new ColonySizeSmall());


        Colony colonyTropicsLarge1 = new Colony(new ColonyOriginTropics(), new ColonySizeLarge());
        Colony colonyTropicsMedium1 = new Colony(new ColonyOriginTropics(), new ColonySizeMedium());
        Colony colonyTropicsSmall1 = new Colony(new ColonyOriginTropics(), new ColonySizeSmall());

        Colony colonyTropicsLarge2 = new Colony(new ColonyOriginTropics(), new ColonySizeLarge());
        Colony colonyTropicsMedium2 = new Colony(new ColonyOriginTropics(), new ColonySizeMedium());
        Colony colonyTropicsSmall2 = new Colony(new ColonyOriginTropics(), new ColonySizeSmall());

        System.out.println("\n1) test add Form to Institute");


        run("\tadd one formicarium:", () -> addFormToInstitute(i1, formLargeRegulated1));
        run("\tadd the equal formicarium:", () -> addEqualFormError(i1, formLargeRegulated1));
        formLargeRegulated2.assign(colonyTropicsLarge1);
        run("\tadd assigned formicarium:", () -> addEqualFormError(i1, formLargeRegulated2));

        System.out.println("\n2) test remove Form from Institute");
        run("\tremove one formicarium:", () -> removeFormFromInstitute(i1, formLargeRegulated1));
        run("\tremove non existing formicarium:", () -> removeFormFromInstituteError(i1, formLargeRegulated1));
        run("\tremove occupied formicarium:", () -> removeFormFromInstituteError(i1, formLargeRegulated2));
        i1.addForm(formLargeRegulated1);
        formLargeRegulated2.unassign();


        i1.addForm(formLargeRegulated2);
        i1.addForm(formLargeUnregulated1);
        i1.addForm(formSmallRegulated1);
        i1.addForm(formSmallUnregulated2);

        i2.addForm(formMediumRegulated1);
        i2.addForm(formMediumUnregulated1);
        i2.addForm(formSmallRegulated2);
        i2.addForm(formLargeUnregulated2);

        i3.addForm(formSmallRegulated1);
        i3.addForm(formMediumRegulated2);
        i3.addForm(formLargeRegulated1);

        run("\n3) test showFormicariums in Institute", () -> showFormicariumsInInstitute(i1));

        run("\n4) test priceFree in Institute (in this case must be 40)", i1::priceFree);

        System.out.println("\n\n5) test assignForm in Institute i1\n");

        run("\n\t 5.1 add european ants with large size, they should occupy large, unregulated formicarium", () -> addColonyToInstitute(i1, colonyEuropeLarge1));
        run("\n\t 5.2 add tropical ants with small size, they should occupy small, regulated formicarium", () -> addColonyToInstitute(i1, colonyTropicsSmall1));
        run("\n\t 5.3 add european ants with small size, they should occupy small, unregulated formicarium", () -> addColonyToInstitute(i1, colonyEuropeSmall1));
        run("\n\t 5.4 add tropic ants with small size, they should NOT occupy LARGE, regulated formicarium", () -> addColonyToInstitute(i1, colonyTropicsSmall2));
        run("\n\t 5.5 add european ants with large size, they should not be in institute, because all suitable formicariums are occupied", () -> addColonyToInstitute(i1, colonyEuropeLarge2));
        run("\n\t 5.6 add tropic ants with medium size, they should occupy LARGE, regulated formicarium", () -> addColonyToInstitute(i1, colonyTropicsMedium1));

        System.out.println("\n\n6) test assignForm in Institute i2\n");

        run("\n\t 6.1 add tropical ants with small size, they should occupy small, regulated formicarium", () -> addColonyToInstitute(i2, colonyTropicsSmall2));
        run("\n\t 6.2 add european ants with medium size, they should occupy LARGE, unregulated formicarium", () -> addColonyToInstitute(i2, colonyEuropeMedium1));
        run("\n\t 6.3 add european ants with large size, they should occupy large, unregulated formicarium", () -> addColonyToInstitute(i2, colonyEuropeLarge2));
        run("\n\t 6.4 add european ants with medium size, they should not be in institute, because all suitable formicariums are occupied", () -> addColonyToInstitute(i2, colonyEuropeMedium1));

        System.out.println("\n\n7) test assignForm in Institute i3\n");

        run("\n\t 7.1 add european ants with small size, they should not be in institute, because all suitable formicariums are occupied", () -> addColonyToInstitute(i3, colonyEuropeSmall1));
        run("\n\t 7.1 add european ants with medium size, they should not be in institute, because all suitable formicariums are occupied", () -> addColonyToInstitute(i3, colonyEuropeMedium1));
        run("\n\t 7.1 add european ants with large size, they should not be in institute, because all suitable formicariums are occupied", () -> addColonyToInstitute(i3, colonyEuropeLarge1));

        run("\n8) test priceFree in Institute (in this case must be 9.7)", i1::priceFree);
        run("\n9) test priceOccupied in Institute (in this case must be 30.3)", i1::priceOccupied);
        run("\n10) test returnForm", () -> returnForm(i1, formLargeUnregulated1));

        run("\n11) test catch exception with null parameter", () -> catchException(RuntimeException.class, () -> formLargeRegulated1.assign(null)));

        System.out.println("\n\n--- Context B ----");
        try {
            Class<?> aspect = Test.class.getClassLoader().loadClass("MethodCounterAspect");
            int assignCount = (int) aspect.getMethod("formAssignCounter").invoke(null);
            int visitorCount = (int) aspect.getMethod("visitorCounter").invoke(null);
            System.out.println("\nNumber of calls for formAssign: " + assignCount);
            System.out.println("Number of calls for visitors: " + visitorCount);

        } catch (ClassNotFoundException e) {
            System.out.println("Context B not loaded, compile with ajc");
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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

    private static void addFormToInstitute(Institute i, Form form) {
        i.addForm(form);
        checkEquals(i.sizeOfInventory(), 1);
    }

    private static void addEqualFormError(Institute i, Form form) {
        checkError(() -> i.addForm(form));
    }

    private static void removeFormFromInstitute(Institute i, Form form) {
        i.deleteForm(form);
        checkEquals(i.sizeOfInventory(), 0);
    }

    private static void removeFormFromInstituteError(Institute i, Form form) {
        checkError(() -> i.deleteForm(form));
    }

    private static void showFormicariumsInInstitute(Institute i) {
        i.showFormicarium();
    }

    private static void addColonyToInstitute(Institute i, Colony c) {
        System.out.println("\nall formicariums in the inventory bevor assignForm");
        i.showFormicarium();
        Form form = i.assignForm(c);
        if (form != null)
            form.assign(c);
        System.out.println("\nall formicariums in the inventory after assignForm");
        i.showFormicarium();
        System.out.println("\nants in formicariums");
        i.showAnts();
    }

    private static void returnForm(Institute institute, Form form) {
        Colony c = form.colony();
        institute.returnForm(form);
        checkEquals(institute.sizeOfOccupied(), 3);
        checkEquals(institute.sizeOfInventory(), 2);
        checkEqualsWithNull(c.form());
        checkEqualsWithNull(form.colony());
    }

    private static void checkEquals(double value, double expected) {
        if (expected != value)
            throw new AssertionError("provided value " + value + " not equal with expected value " + expected);
    }

    private static void checkError(Runnable func) {
        boolean any = false;
        try {
            func.run();
        } catch (RuntimeException e) {
            any = true;
        } catch (Throwable throwable) {
            throw new AssertionError("Exception check failed, wrong execution", throwable);
        }
        if (!any) throw new AssertionError("Exception check failed");
    }

    private static void checkEqualsWithNull(Object provided) {
        if (provided != null)
            throw new AssertionError("provided value is not null");
    }

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
}