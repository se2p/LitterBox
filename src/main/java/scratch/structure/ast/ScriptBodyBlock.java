package scratch.structure.ast;

public abstract class ScriptBodyBlock extends BasicBlock implements Stackable, Extendable {

    public ScriptBodyBlock(String opcode, Extendable parent, Stackable next) {
        super(opcode, parent, next);
    }
}
