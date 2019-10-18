package scratch.structure.ast.bool;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Input;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;

public abstract class BooleanBlock extends ScratchBlock implements Input {

    public BooleanBlock(String opcode, Extendable parent, Stackable next) {
        super(opcode, parent, next);
    }
}
