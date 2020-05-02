package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.LinkedHashSet;
import java.util.Set;

public class ListUseVisitor implements ScratchVisitor {


    private Set<Identifier> uses = new LinkedHashSet<>();

    public Set<Identifier> getUses() {
        return uses;
    }


    @Override
    public void visit(AddTo stmt) {
        uses.add(stmt.getIdentifier());
    }

    @Override
    public void visit(DeleteOf stmt) {
        uses.add(stmt.getIdentifier());
        stmt.getNum().accept(this);
    }

    @Override
    public void visit(DeleteAllOf stmt) {
        // No use
    }

    @Override
    public void visit(InsertAt stmt) {
        uses.add(stmt.getIdentifier());
        stmt.getIndex().accept(this);
    }

    @Override
    public void visit(ReplaceItem stmt) {
        uses.add(stmt.getIdentifier());
        stmt.getIndex().accept(this);
    }

    @Override
    public void visit(IfThenStmt node) {
        node.getBoolExpr().accept(this);
    }

    @Override
    public void visit(IfElseStmt node) {
        node.getBoolExpr().accept(this);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        // Nop
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        node.getTimes().accept(this);
    }

    @Override
    public void visit(UntilStmt node) {
        node.getBoolExpr().accept(this);
    }

    @Override
    public void visit(AttributeOf node) {
        // It seems AttributeOf cannot refer to lists
    }

    @Override
    public void visit(Qualified node) {
        if(node.getSecond() instanceof ScratchList) {
            uses.add(node);
        }
    }
}
