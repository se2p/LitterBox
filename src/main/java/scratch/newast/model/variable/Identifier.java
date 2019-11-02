package scratch.newast.model.variable;

public class Identifier implements Variable {
    private String value; // TODO check if this is correct

    public Identifier(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}