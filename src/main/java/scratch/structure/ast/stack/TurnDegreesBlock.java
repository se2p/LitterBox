package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class TurnDegreesBlock extends SingleIntInputBlock {

    public TurnDegreesBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, String inputName, int inputValue) {
        super(opcode, next, parent, shadow, topLevel, inputName, inputValue);
    }
}
