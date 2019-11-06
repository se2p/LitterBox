package scratch.newast.model.statement.spritemotion;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class MoveSteps implements SpriteMotionStmt {
    private final NumExpr steps;
    private final ImmutableList<ASTNode> children;

    public MoveSteps(NumExpr steps) {
        this.steps = steps;
        children = ImmutableList.<ASTNode>builder().add(steps).build();
    }

    public NumExpr getSteps() {
        return steps;
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