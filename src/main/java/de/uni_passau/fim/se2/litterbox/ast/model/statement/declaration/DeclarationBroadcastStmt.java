package de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class DeclarationBroadcastStmt extends AbstractNode implements DeclarationStmt {

    private final LocalIdentifier ident;
    private final Type type;

    public DeclarationBroadcastStmt(LocalIdentifier ident, Type type) {
        super(ident, type);
        this.ident = Preconditions.checkNotNull(ident);
        this.type = Preconditions.checkNotNull(type);
    }

    public LocalIdentifier getIdent() {
        return ident;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}

