package de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ChangeVolumeBy extends AbstractNode implements ActorSoundStmt {
    private NumExpr volumeValue;

    public ChangeVolumeBy(NumExpr volumeValue) {
        super(volumeValue);
        this.volumeValue = volumeValue;
    }

    public NumExpr getVolumeValue() {
        return volumeValue;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}