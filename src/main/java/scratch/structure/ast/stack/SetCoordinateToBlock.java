package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;

public abstract class SetCoordinateToBlock extends SingleIntInputBlock {

    public SetCoordinateToBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer inputType, String inputName, Integer inputValue, Integer inputShadow) {
        super(opcode, next, parent, shadow, topLevel, inputType, inputName, inputValue, inputShadow);
    }

    public SetCoordinateToBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y, Integer inputType, String inputName, Integer inputValue, Integer inputShadow) {
        super(opcode, next, parent, shadow, topLevel, x, y, inputType, inputName, inputValue, inputShadow);
    }

    public SetCoordinateToBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer inputType, String inputName, String inputVariableID, Integer inputShadow) {
        super(opcode, next, parent, shadow, topLevel, inputType, inputName, inputVariableID, inputShadow);
    }

    public SetCoordinateToBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y, Integer inputType, String inputName, String inputVariableID, Integer inputShadow) {
        super(opcode, next, parent, shadow, topLevel, x, y, inputType, inputName, inputVariableID, inputShadow);
    }
}
