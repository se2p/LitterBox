package scratch.structure.ast.hat;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class WhenSpriteClickedBlock extends HatBlock {

    public WhenSpriteClickedBlock(String opcode, Stackable next, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, next, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}

