package scratch.structure.ast.stack;

public abstract class SetCoordinateToBlock extends SingleIntInputBlock {


    public SetCoordinateToBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public SetCoordinateToBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }
}
