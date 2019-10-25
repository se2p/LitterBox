package scratch.structure.ast;

public abstract class ScriptBodyBlock extends ScratchBlock implements Stackable, Extendable {

    Extendable parent = null;
    Stackable next = null;

    public ScriptBodyBlock(String opcode, String id) {
        super(opcode, id);
    }

    @Override
    public Extendable getParent() {
        return parent;
    }

    @Override
    public void setParent(Extendable parent) {
        this.parent = parent;
    }

    @Override
    public Stackable getNext() {
        return next;
    }

    @Override
    public void setNext(Stackable next) {
        this.next = next;
    }
}
