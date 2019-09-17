package scratch.structure.ast.stack;

import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScriptBodyBlock;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class MoveStepBlock extends ScriptBodyBlock implements BasicBlock {

    private final int inputType = 4; // Type of input in the inputs array according to file format
    private String opcode;
    private Stackable next;
    private Extendable parent;
    private int inputShadow;
    private int steps;
    private boolean shadow;
    private boolean topLevel;
    private int x;
    private int y;

    public MoveStepBlock(String opcode, Stackable next, Extendable parent, int steps, int inputShadow, boolean shadow, boolean topLevel) {
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.steps = steps;
        this.inputShadow = inputShadow;
        this.shadow = shadow;
        this.topLevel = topLevel;
    }

    public MoveStepBlock(String opcode, Stackable next, Extendable parent, int steps, int inputShadow,  boolean shadow, boolean topLevel, int x, int y) {
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.steps = steps;
        this.inputShadow = inputShadow;
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

    public BasicBlock getParent() {
        return (BasicBlock) parent;
    }

    public int getInputType() {
        return inputType;
    }

    public int getInputShadow() {
        return inputShadow;
    }

    public int getSteps() {
        return steps;
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
