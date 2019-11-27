package scratch.ast.model.statement.pen;

import scratch.ast.model.ASTLeaf;
import scratch.ast.model.AbstractNode;
import scratch.ast.visitor.ScratchVisitor;

public class PenStampStmt extends AbstractNode implements PenStmt, ASTLeaf {
    public PenStampStmt() {
        super();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
