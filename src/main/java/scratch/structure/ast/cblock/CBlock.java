package scratch.structure.ast.cblock;

import scratch.structure.ast.*;
import scratch.structure.ast.visitor.BlockVisitor;

public abstract class CBlock extends ScratchBlock  {

    protected ScriptBodyBlock substack; //First block of the substack // TODO: Should we use a slot for this?

    public CBlock(String opcode, Extendable parent, Stackable next, ScriptBodyBlock substack, Boolean shadow, Boolean topLevel) {
        super(opcode, parent, next);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.substack = substack;
    }

    public CBlock(String opcode, Extendable parent, Stackable next, ScriptBodyBlock substack, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, parent, next);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
        this.substack = substack;
    }

    public ScriptBodyBlock getSubstack() {
        return substack;
    }

    public void setSubstack(ScriptBodyBlock substack) {
        this.substack = substack;
    }
}
