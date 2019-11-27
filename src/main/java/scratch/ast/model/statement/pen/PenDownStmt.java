package scratch.ast.model.statement.pen;

import scratch.ast.model.ASTLeaf;
import scratch.ast.model.AbstractNode;
import scratch.ast.visitor.ScratchVisitor;

public class PenDownStmt extends AbstractNode implements PenStmt, ASTLeaf {
    public PenDownStmt() {
        super();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
