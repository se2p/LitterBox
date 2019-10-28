package scratch.structure.ast.bool;

import scratch.structure.ast.inputs.Slot;
import scratch.structure.ast.visitor.BlockVisitor;

//TODO check if we should name this one also OperatorGTBlock?
public class OperatorGT extends BooleanBlock {

    Slot operand1 = null;
    Slot operand2 = null;

    public OperatorGT(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public OperatorGT(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }


    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }

    public Slot getOperand1() {
        return operand1;
    }

    public void setOperand1(Slot operand1) {
        this.operand1 = operand1;
    }

    public Slot getOperand2() {
        return operand2;
    }

    public void setOperand2(Slot operand2) {
        this.operand2 = operand2;
    }

    @Override
    public String toString() {
        return  operand1 + "," +
                operand2 + ", [" + x + "," + y + "]";
    }
}
