package scratch.newast.model.variable;

public class Concat extends Variable {
    private Identifier first;
    private Identifier second;

    public Concat(Identifier first, Identifier second) {
        this.first = first;
        this.second = second;
    }

    public Identifier getFirst() {
        return first;
    }

    public void setFirst(Identifier first) {
        this.first = first;
    }

    public Identifier getSecond() {
        return second;
    }

    public void setSecond(Identifier second) {
        this.second = second;
    }
}