package de.uni_passau.fim.se2.litterbox.ast.model.variable;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class DataExpr extends AbstractNode implements Expression {
    private LocalIdentifier name;

    public DataExpr(LocalIdentifier name) {
        super(name);
        this.name = name;
    }

    public LocalIdentifier getName() {
        return name;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
