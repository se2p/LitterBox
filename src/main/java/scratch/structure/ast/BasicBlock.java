package scratch.structure.ast;

import scratch.structure.ast.visitor.BlockVisitor;

public abstract class BasicBlock {

    private String opcode;
    private Extendable parent;
    private Stackable next;
    protected boolean shadow;
    protected boolean topLevel;
    protected int x;
    protected int y;

    public BasicBlock(String opcode, Extendable parent, Stackable next) {
        this.opcode = opcode;
        this.parent = parent;
        this.next = next;
    }

    public abstract void accept(BlockVisitor visitor);

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public Extendable getParent() {
        return parent;
    }

    public void setParent(Extendable parent) {
        this.parent = parent;
    }

    public Stackable getNext() {
        return next;
    }

    public void setNext(Stackable next) {
        this.next = next;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
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
}
