package scratch.structure.ast.bool;

import scratch.structure.ast.Input;
import scratch.structure.ast.ScratchBlock;

public abstract class BooleanBlock extends ScratchBlock implements Input {

    public BooleanBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id);
        this.shadow = shadow;
        this.topLevel = topLevel;
    }

    public BooleanBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }

}
