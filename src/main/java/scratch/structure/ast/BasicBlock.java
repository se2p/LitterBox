package scratch.structure.ast;

import scratch.structure.ast.visitor.BlockVisitor;

public interface BasicBlock {

    BasicBlock getParent();

    void accept(BlockVisitor visitor);

    void setParent(BasicBlock basicBlock);

    BasicBlock getNext();

    void setNext(BasicBlock basicBlock);
}
