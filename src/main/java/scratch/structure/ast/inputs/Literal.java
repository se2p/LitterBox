package scratch.structure.ast.inputs;

import scratch.structure.ast.Input;

public class Literal implements Input {

    private int type;
    private String value;

    public Literal(int type, String value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return type + ", " + value;
    }
}
