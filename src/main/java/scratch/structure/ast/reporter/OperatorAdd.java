package scratch.structure.ast.reporter;

import scratch.structure.ast.inputs.Slot;
import scratch.structure.ast.visitor.BlockVisitor;

//TODO check if we should name this one also OperatorAddBlock?
public class OperatorAdd extends ReporterBlock {

    Slot num1 = null;
    Slot num2 = null;

    public OperatorAdd(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public OperatorAdd(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }


    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }

    public Slot getNum1() {
        return num1;
    }

    public void setNum1(Slot num1) {
        this.num1 = num1;
    }

    public Slot getNum2() {
        return num2;
    }

    public void setNum2(Slot num2) {
        this.num2 = num2;
    }

    @Override
    public String toString() {
        return  num1 + "," +
                 num2 ;
    }
}
