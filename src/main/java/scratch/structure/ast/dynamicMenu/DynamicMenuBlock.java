package scratch.structure.ast.dynamicMenu;

import scratch.structure.ast.Input;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.visitor.BlockVisitor;

public class DynamicMenuBlock extends ScratchBlock implements Input {

    public DynamicMenuBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id);
        this.shadow = shadow;
        this.topLevel = topLevel;
    }


    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
