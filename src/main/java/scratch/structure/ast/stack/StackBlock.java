package scratch.structure.ast.stack;

import scratch.structure.ast.ScriptBodyBlock;

public abstract class StackBlock extends ScriptBodyBlock {


    public StackBlock(String opcode, boolean shadow, boolean topLevel) {
        super(opcode);
        this.shadow = shadow;
        this.topLevel = topLevel;
    }

    public StackBlock(String opcode, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }
}
