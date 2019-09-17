package scratch.structure.ast.hat;

import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class WhenFlagClickedBlock extends HatBlock {

    private Stackable next;

    public WhenFlagClickedBlock(String opcode, Stackable next, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, next, shadow, topLevel, x, y);
        this.next = next;
    }

    @Override
    public BasicBlock getParent() {
        return null;
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setParent(BasicBlock basicBlock) {
        throw new NotImplementedException();
    }

    @Override
    public void setNext(Stackable next) {
        this.next = next;
    }
}
