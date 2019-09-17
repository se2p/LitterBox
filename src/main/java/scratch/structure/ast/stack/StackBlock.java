package scratch.structure.ast.stack;

import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScriptBodyBlock;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public abstract class StackBlock extends ScriptBodyBlock implements BasicBlock {

    private String opcode;
    private Stackable next;
    private Extendable parent;
//    private Object[] inputs; //Todo: Make this more specific, once we have proper types for inputs
//    private Object[] fields; //Todo: Make this more specific, once we have proper types for fields
    private boolean shadow;
    private boolean topLevel;
    private int x;
    private int y;

    public StackBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel) {
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.shadow = shadow;
        this.topLevel = topLevel;
    }

    public StackBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int x, int y) {
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }

    public void setNext(Stackable next) {
        this.next = next;
    }

    public void setParent(BasicBlock parent) {
        //todo add type check
        this.parent = (Extendable) parent;
    }

    public String getOpcode() {
        return opcode;
    }

    public Stackable getNext() {
        return next;
    }

    @Override
    public void setNext(BasicBlock basicBlock) {
        //todo add type check
        this.next = (Stackable) basicBlock;
    }

    public BasicBlock getParent() {
        return (BasicBlock) parent;
    }

    public boolean isShadow() {
        return shadow;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
