package scratch.structure.ast.inputs;

import scratch.structure.ast.Input;

public class ListBlock implements Input {

    private int type; // Is always Variable
    private String name;
    private String reference;
    private int x;
    private int y;

    public ListBlock(int type, String name, String reference) {
        this.type = type;
        this.name = name;
        this.reference = reference;
    }

    public ListBlock(int type, String name, String reference, int x, int y) {
        this.type = type;
        this.name = name;
        this.reference = reference;
        this.x = x;
        this.y = y;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return type + ", " + name + ", " + reference;
    }
}
