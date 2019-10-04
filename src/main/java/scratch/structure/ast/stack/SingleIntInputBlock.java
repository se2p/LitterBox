package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;

public abstract class SingleIntInputBlock extends StackBlock {

    private String inputName;
    private int inputValue;

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, String inputName, int inputValue) {
        super(opcode, parent, next, shadow, topLevel);
        this.inputName = inputName;
        this.inputValue = inputValue;
    }

    public SingleIntInputBlock(String opcode, Stackable next, Extendable parent, boolean shadow, boolean topLevel, int x, int y, String inputName, int inputValue) {
        super(opcode, parent, next, shadow, topLevel, x, y);
        this.inputName = inputName;
        this.inputValue = inputValue;
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
}
