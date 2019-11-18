package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.string.StringExpr;

public class Say implements SpriteLookStmt {
    private final StringExpr string;
    private final ImmutableList<ASTNode> children;

    public Say(StringExpr string) {
        this.string = string;
        children = ImmutableList.<ASTNode>builder().add(string).build();
    }

    public StringExpr getString() {
        return string;
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