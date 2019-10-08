package scratch.structure.ast.cap;

import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;

public abstract class CapBlock extends BasicBlock {

    public CapBlock(String opcode, Extendable parent) {
        super(opcode, parent, null);
    }
}
