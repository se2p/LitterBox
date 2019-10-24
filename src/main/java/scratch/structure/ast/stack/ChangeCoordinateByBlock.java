package scratch.structure.ast.stack;

public abstract class ChangeCoordinateByBlock extends SingleIntInputBlock {

    public ChangeCoordinateByBlock(String opcode, Boolean shadow, Boolean topLevel) {
        super(opcode, shadow, topLevel);
    }

    public ChangeCoordinateByBlock(String opcode, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, shadow, topLevel, x, y);
    }
}
