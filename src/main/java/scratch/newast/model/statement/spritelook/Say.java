package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Say implements SpriteLookStmt {
    private final String string;
    private final ImmutableList<ASTNode> children;

    public Say(String string) {
        this.string = string;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public String getString() {
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