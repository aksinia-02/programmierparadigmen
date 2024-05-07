public aspect MethodCounterAspect {
    private static int formAssignCounter = 0;
    private static int visitorCounter = 0;

    after(): call(* Institute.assignForm(..)) {
        formAssignCounter++;
    }

    after(): call(* ColonySize.isCompatible(..)) {
        visitorCounter++;
    }
    after(): call(* ColonySize.prefersSmallForm(..)) {
        visitorCounter++;
    }
    after(): call(* ColonySize.prefersMediumFrom(..)) {
        visitorCounter++;
    }
    after(): call(* ColonySize.prefersLargeForm(..)) {
        visitorCounter++;
    }

    after(): call(* FormSize.preferredBy(..)) {
        visitorCounter++;
    }
    after(): call(* FormSize.supportsSmallColony(..)) {
        visitorCounter++;
    }
    after(): call(* FormSize.supportsMediumColony(..)) {
        visitorCounter++;
    }
    after(): call(* FormSize.supportsLargeColony(..)) {
        visitorCounter++;
    }

    after(): call(* ColonyOrigin.isCompatible(..)) {
        visitorCounter++;
    }
    after(): call(* FormRegulation.supportsEuropeanAnts(..)) {
        visitorCounter++;
    }
    after(): call(* FormRegulation.supportsTropicalAnts(..)) {
        visitorCounter++;
    }

    public static int formAssignCounter() {
        return formAssignCounter;
    }

    public static int visitorCounter() {
        return visitorCounter;
    }
}
