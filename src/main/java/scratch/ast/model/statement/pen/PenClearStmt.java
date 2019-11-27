package scratch.ast.model.statement.pen;

import scratch.ast.model.ASTLeaf;
import scratch.ast.model.AbstractNode;
import scratch.ast.visitor.ScratchVisitor;

public class PenClearStmt extends AbstractNode implements PenStmt, ASTLeaf {
    public PenClearStmt() {
        super();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
