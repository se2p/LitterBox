package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;

public abstract class SetCoordinateToBlock extends SingleIntInputBlock {

    public SetCoordinateToBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel) {
        super(opcode, next, parent, shadow, topLevel);
    }

    public SetCoordinateToBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, next, parent, shadow, topLevel, x, y);
    }
}
