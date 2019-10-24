package scratch.structure.ast.reporter;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Input;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;

public abstract class ReporterBlock extends ScratchBlock implements Input {

    Stackable next = null;
    Extendable parent = null;

    public ReporterBlock(String opcode) {
        super(opcode);
    }

    public Stackable getNext() {
        return next;
    }

    public void setNext(Stackable next) {
        this.next = next;
    }

    public Extendable getParent() {
        return parent;
    }

    public void setParent(Extendable parent) {
        this.parent = parent;
    }
}
