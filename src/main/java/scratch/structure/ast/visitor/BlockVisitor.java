package scratch.structure.ast.visitor;

import scratch.structure.ast.BasicBlock;

public interface BlockVisitor {

    void visit(BasicBlock block);
}
