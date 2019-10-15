package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class PointInDirectionBlock extends SingleIntInputBlock {

    public PointInDirectionBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer inputType, String inputName, Integer inputValue, Integer inputShadow) {
        super(opcode, next, parent, shadow, topLevel, inputType, inputName, inputValue, inputShadow);
    }

    public PointInDirectionBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y, Integer inputType, String inputName, Integer inputValue, Integer inputShadow) {
        super(opcode, next, parent, shadow, topLevel, x, y, inputType, inputName, inputValue, inputShadow);
    }

    public PointInDirectionBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer inputType, String inputName, String inputID, Integer inputShadow) {
        super(opcode, next, parent, shadow, topLevel, inputType, inputName, inputID, inputShadow);
    }

    public PointInDirectionBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y, Integer inputType, String inputName, String inputID, Integer inputShadow) {
        super(opcode, next, parent, shadow, topLevel, x, y, inputType, inputName, inputID, inputShadow);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
