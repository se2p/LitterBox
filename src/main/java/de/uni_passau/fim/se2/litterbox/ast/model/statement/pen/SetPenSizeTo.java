package de.uni_passau.fim.se2.litterbox.ast.model.statement.pen;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class SetPenSizeTo extends AbstractNode implements PenStmt {
    private NumExpr value;

    public SetPenSizeTo(NumExpr value) {
        super(value);
        this.value = Preconditions.checkNotNull(value);
    }

    public NumExpr getValue() {
        return value;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
