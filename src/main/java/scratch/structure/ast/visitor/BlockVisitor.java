package scratch.structure.ast.visitor;

import scratch.structure.ast.ScratchBlock;

public interface BlockVisitor {

    void visit(ScratchBlock block);
}
