package scratch.newast.model.statement.entitylook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.string.StringExpr;

public class AskAndWait implements ActorLookStmt {

    private final StringExpr question;
    private final ImmutableList<ASTNode> children;

    public AskAndWait(StringExpr question) {
        this.question = question;
        children = ImmutableList.<ASTNode>builder().add(question).build();
    }

    public StringExpr getQuestion() {
        return question;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}