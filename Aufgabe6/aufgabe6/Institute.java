package aufgabe6;

@Meta(author = Meta.Author.PRIVAS)
public class Institute {
    private final String name;

    @Invariant("names of formicariums are unique")
    private final LinkedList formicariums = new LinkedList();

    @Meta(author = Meta.Author.VOROBEVA)
    public Institute(String name) {
        this.name = name;
    }

    @Contract(post = "returns amount of formicariums in institute")
    public int formicariumCount() {
        return formicariums.size();
    }

    @Contract(pre = "formicarium must not be null", post = "returns true is formicarium belongs to institute, otherwise false")
    @Meta(author = Meta.Author.VOROBEVA)
    public boolean containsFormicarium(Formicarium formicarium) {
        return formicariums.contains(formicarium);
    }

    @Contract(pre = "formicarium is not null", post = "adds formicarium in Institute if it is not included yet")
    @Meta(author = Meta.Author.VOROBEVA)
    public void addFormicarium(Formicarium formicarium) {
        if (!formicariums.contains(formicarium))
            formicariums.add(formicarium);
    }

    @Contract(post = "removes formicarium with provided Name from Institute if it is already included")
    public void removeFormicarium(String formicariumName) {
        formicariums.remove(formicariumName, (name, formicarium) -> ((Formicarium) formicarium).name().equals(name));
    }

    @Override
    @Meta(author = Meta.Author.VOROBEVA)
    public String toString() {
        StringBuilder result = new StringBuilder("name of institute: " + name + ", contains formicariums: \n");
        for (Object formicarium : formicariums) {
            result.append("\t").append(formicarium.toString()).append("\n");
        }
        return result.toString();
    }
}
