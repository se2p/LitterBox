package scratch.structure.ast;

import scratch.structure.ast.visitor.BlockVisitor;

public abstract class BasicBlock {

    private String opcode;
    private Extendable parent;
    private Stackable next;

    public BasicBlock(String opcode, Extendable parent, Stackable next) {
        this.opcode = opcode;
        this.parent = parent;
        this.next = next;
    }

    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }

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
}
