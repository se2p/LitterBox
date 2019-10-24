package scratch.structure.ast.bool;

import scratch.structure.ast.Input;
import scratch.structure.ast.ScratchBlock;

public abstract class BooleanBlock extends ScratchBlock implements Input {

    public BooleanBlock(String opcode) {
        super(opcode);
    }
}
