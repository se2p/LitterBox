package scratch.structure.ast.reporter;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Input;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;

public abstract class ReporterBlock extends ScratchBlock implements Input {

    public ReporterBlock(String opcode, Extendable parent, Stackable next) {
        super(opcode, parent, next);
    }
}
