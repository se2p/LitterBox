package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.LinkedHashSet;
import java.util.Set;

public class ListDefinitionVisitor implements ScratchVisitor {

    private Set<Identifier> definitions = new LinkedHashSet<>();

    public Set<Identifier> getDefinitions() {
        return definitions;
    }

    @Override
    public void visit(AddTo stmt) {
        definitions.add(stmt.getIdentifier());
    }

    @Override
    public void visit(DeleteAllOf stmt) {
        definitions.add(stmt.getIdentifier());
    }

    @Override
    public void visit(DeleteOf stmt) {
        definitions.add(stmt.getIdentifier());
    }

    @Override
    public void visit(InsertAt stmt) {
        definitions.add(stmt.getIdentifier());
    }

    @Override
    public void visit(ReplaceItem stmt) {
        definitions.add(stmt.getIdentifier());
    }

    @Override
    public void visit(Stmt node) {
        // Nop
    }
}
