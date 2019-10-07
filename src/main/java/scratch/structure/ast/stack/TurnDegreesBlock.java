package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class TurnDegreesBlock extends SingleIntInputBlock {

    public TurnDegreesBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, String inputName, int inputValue) {
        super(opcode, next, parent, shadow, topLevel, inputName, inputValue);
    }

    public TurnDegreesBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int x, int y, String inputName, int inputValue) {
        super(opcode, next, parent, shadow, topLevel, x, y, inputName, inputValue);
    }

    public TurnDegreesBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, String inputName, String inputVariableID) {
        super(opcode, next, parent, shadow, topLevel, inputName, inputVariableID);
    }

    public TurnDegreesBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int x, int y, String inputName, String inputVariableID) {
        super(opcode, next, parent, shadow, topLevel, x, y, inputName, inputVariableID);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
