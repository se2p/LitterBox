package de.uni_passau.fim.se2.litterbox.ast.model.expression.bool;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class ListContains extends AbstractNode implements BoolExpr {
    private Expression element;
    private Identifier identifier;

    public ListContains(Identifier identifier, Expression element) {
        super(identifier, element);
        this.element = element;
        this.identifier = Preconditions.checkNotNull(identifier);
    }

    public Expression getElement() {
        return element;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}