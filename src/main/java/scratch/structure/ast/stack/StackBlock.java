package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScriptBodyBlock;
import scratch.structure.ast.Stackable;

public abstract class StackBlock extends ScriptBodyBlock implements Stackable {

    public StackBlock(String opcode, Extendable parent, Stackable next, boolean shadow, boolean topLevel) {
        super(opcode, parent, next);
        this.shadow = shadow;
        this.topLevel = topLevel;
    }

    public StackBlock(String opcode, Extendable parent, Stackable next, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, parent, next);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }
}
