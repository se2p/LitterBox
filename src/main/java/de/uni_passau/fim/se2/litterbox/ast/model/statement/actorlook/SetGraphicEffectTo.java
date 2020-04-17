package de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SetGraphicEffectTo extends AbstractNode implements ActorLookStmt {
    private NumExpr value;
    private GraphicEffect effect;

    public SetGraphicEffectTo(GraphicEffect effect, NumExpr value) {
        super(effect, value);
        this.value = value;
        this.effect = effect;
    }

    public NumExpr getValue() {
        return value;
    }

    public GraphicEffect getEffect() {
        return effect;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
