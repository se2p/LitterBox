package scratch.ast.model.statement;

import scratch.ast.model.ASTLeaf;
import scratch.ast.model.AbstractNode;
import scratch.ast.visitor.ScratchVisitor;

public class UnspecifiedStmt extends AbstractNode implements ASTLeaf, Stmt {

    public UnspecifiedStmt() {
        super();

    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
