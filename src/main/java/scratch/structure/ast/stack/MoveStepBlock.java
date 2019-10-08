package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScriptBodyBlock;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class MoveStepBlock extends ScriptBodyBlock {

    private final int inputType = 4; // Type of input in the inputs array according to file format
    private int inputShadow;
    private int steps;

    public MoveStepBlock(String opcode, Extendable parent, Stackable next, int steps, int inputShadow, boolean shadow, boolean topLevel) {
        this(opcode, parent, next, steps, inputShadow, shadow, topLevel, 0, 0);
    }

    public MoveStepBlock(String opcode,  Extendable parent, Stackable next,int steps, int inputShadow,  boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, parent, next);
        this.steps = steps;
        this.inputShadow = inputShadow;
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
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

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
