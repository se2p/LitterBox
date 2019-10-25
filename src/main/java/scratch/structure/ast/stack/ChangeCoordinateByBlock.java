package scratch.structure.ast.stack;

public abstract class ChangeCoordinateByBlock extends SingleIntInputBlock {

    public ChangeCoordinateByBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public ChangeCoordinateByBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }
}
