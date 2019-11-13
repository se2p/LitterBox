package scratch.newast.model.statement.entitylook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.graphiceffect.GraphicEffect;

public class SetEffectTo implements ActorLookStmt {
    private final GraphicEffect effect;
    private final NumExpr num;
    private final ImmutableList<ASTNode> children;

    public SetEffectTo(GraphicEffect effect, NumExpr num) {
        this.effect = effect;
        this.num = num;
        children = ImmutableList.<ASTNode>builder().add(effect).add(num).build();
    }

    public GraphicEffect getEffect() {
        return effect;
    }

    public NumExpr getNum() {
        return num;
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