package scratch.structure.ast.stack;

public abstract class SetCoordinateToBlock extends SingleIntInputBlock {
    public SetCoordinateToBlock(String opcode, Boolean shadow, Boolean topLevel) {
        super(opcode, shadow, topLevel);
    }

    public SetCoordinateToBlock(String opcode, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, shadow, topLevel, x, y);
    }
}
