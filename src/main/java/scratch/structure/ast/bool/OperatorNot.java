package scratch.structure.ast.bool;

import scratch.structure.ast.inputs.Slot;
import scratch.structure.ast.visitor.BlockVisitor;

public class OperatorNot extends BooleanBlock {

    Slot condition;

    public OperatorNot(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public OperatorNot(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    public Slot getCondition() {
        return condition;
    }

    public void setCondition(Slot condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return  condition + ", [" + x + "," + y + "]";
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
