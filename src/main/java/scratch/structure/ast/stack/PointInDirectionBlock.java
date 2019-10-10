package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class PointInDirectionBlock extends SingleIntInputBlock {


    public PointInDirectionBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int inputType, String inputName, int inputValue, int inputShadow) {
        super(opcode, next, parent, shadow, topLevel, inputType, inputName, inputValue, inputShadow);
    }

    public PointInDirectionBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int x, int y, int inputType, String inputName, int inputValue, int inputShadow) {
        super(opcode, next, parent, shadow, topLevel, x, y, inputType, inputName, inputValue, inputShadow);
    }

    public PointInDirectionBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int inputType, String inputName, String inputVariableID, int inputShadow) {
        super(opcode, next, parent, shadow, topLevel, inputType, inputName, inputVariableID, inputShadow);
    }

    public PointInDirectionBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int x, int y, int inputType, String inputName, String inputVariableID, int inputShadow) {
        super(opcode, next, parent, shadow, topLevel, x, y, inputType, inputName, inputVariableID, inputShadow);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
