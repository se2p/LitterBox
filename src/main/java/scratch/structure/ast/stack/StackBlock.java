package scratch.structure.ast.stack;

import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScriptBodyBlock;
import scratch.structure.ast.Stackable;

public abstract class StackBlock extends BasicBlock implements ScriptBodyBlock {

    private String opcode;
    private Stackable next;
    private Extendable parent;
    private Object[] inputs; //Todo: Make this more specific, once we have proper types for inputs
    private Object[] fields; //Todo: Make this more specific, once we have proper types for fields
    private boolean shadow;
    private boolean topLevel;
    private int x;
    private int y;

    public StackBlock(String opcode, Stackable next, Extendable parent, Object[] inputs, Object[] fields, boolean shadow, boolean topLevel) {
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.inputs = inputs;
        this.fields = fields;
        this.shadow = shadow;
        this.topLevel = topLevel;
    }

    public StackBlock(String opcode, Stackable next, Extendable parent, Object[] inputs, Object[] fields, boolean shadow, boolean topLevel, int x, int y) {
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.inputs = inputs;
        this.fields = fields;
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }

    public String getOpcode() {
        return opcode;
    }

    public Stackable getNext() {
        return next;
    }

    public Extendable getParent() {
        return parent;
    }

    public Object[] getInputs() {
        return inputs;
    }

    public Object[] getFields() {
        return fields;
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
}
