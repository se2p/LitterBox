package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;

public abstract class SingleIntInputBlock extends StackBlock {

    private String inputName;
    private int inputValue;
    private int inputShadow;
    private String inputVariableID;
    private final int inputType = 4; // TODO: Is this always true?

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, String inputName, int inputValue, int inputShadow) {
        super(opcode, parent, next, shadow, topLevel);
        this.inputName = inputName;
        this.inputValue = inputValue;
        this.inputShadow = inputShadow;
    }

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int x, int y, String inputName, int inputValue, int inputShadow ) {
        super(opcode, parent, next, shadow, topLevel, x, y);
        this.inputName = inputName;
        this.inputValue = inputValue;
        this.inputShadow = inputShadow;
    }

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, String inputName, String inputVariableID, int inputShadow) {
        super(opcode, parent, next, shadow, topLevel);
        this.inputName = inputName;
        this.inputVariableID = inputVariableID;
        this.inputShadow = inputShadow;
    }

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int x, int y, String inputName, String inputVariableID, int inputShadow) {
        super(opcode, parent, next, shadow, topLevel, x, y);
        this.inputName = inputName;
        this.inputVariableID = inputVariableID;
        this.inputShadow = inputShadow;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public int getInputValue() {
        return inputValue;
    }

    public void setInputValue(int inputValue) {
        this.inputValue = inputValue;
    }

    public String getInputVariableID() {
        return inputVariableID;
    }

    public void setInputVariableID(String inputVariableID) {
        this.inputVariableID = inputVariableID;
    }

    public int getInputType() {
        return inputType;
    }

    public int getInputShadow() {
        return inputShadow;
    }
}
