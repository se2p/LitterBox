package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;

public abstract class SingleIntInputBlock extends StackBlock {

    private int inputType;
    private String inputName;
    private int inputValue;
    private int inputShadow;
    private String inputID;
    private int shadowType;
    private int shadowValue;

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer inputType, String inputName, Integer inputValue, Integer inputShadow) {
        super(opcode, parent, next, shadow, topLevel);
        this.inputType = inputType;
        this.inputName = inputName;
        this.inputValue = inputValue;
        this.inputShadow = inputShadow;
    }

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y, Integer inputType, String inputName, Integer inputValue, Integer inputShadow ) {
        super(opcode, parent, next, shadow, topLevel, x, y);
        this.inputType = inputType;
        this.inputName = inputName;
        this.inputValue = inputValue;
        this.inputShadow = inputShadow;
    }

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer inputType, String inputName, String inputID, Integer inputShadow) {
        super(opcode, parent, next, shadow, topLevel);
        this.inputType = inputType;
        this.inputName = inputName;
        this.inputID = inputID;
        this.inputShadow = inputShadow;
    }

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y, Integer inputType, String inputName, String inputID, Integer inputShadow) {
        super(opcode, parent, next, shadow, topLevel, x, y);
        this.inputType = inputType;
        this.inputName = inputName;
        this.inputID = inputID;
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

    public String getInputID() {
        return inputID;
    }

    public void setInputID(String inputID) {
        this.inputID = inputID;
    }

    public int getInputType() {
        return inputType;
    }

    public int getInputShadow() {
        return inputShadow;
    }

    public int getShadowType() {
        return shadowType;
    }

    public void setShadowType(int shadowType) {
        this.shadowType = shadowType;
    }

    public int getShadowValue() {
        return shadowValue;
    }

    public void setShadowValue(int shadowValue) {
        this.shadowValue = shadowValue;
    }
}
