/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;

import java.util.LinkedHashSet;
import java.util.Set;

public class VariableUseVisitor implements DefinableCollector<Variable> {

    private ActorDefinition actor;
    private Set<Variable> uses = new LinkedHashSet<>();

    public VariableUseVisitor() {
        this(null);
    }

    public VariableUseVisitor(ActorDefinition actor) {
        this.actor = actor;
    }

    @Override
    public Set<Variable> getDefineables() {
        return uses;
    }

    @Override
    public void visit(SetVariableTo node) {
        // Skip variable as that's a def, only visit expression
        node.getExpr().accept(this);
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
    public void visit(ShowVariable node) {
        // Nop
    }

    @Override
    public void visit(HideVariable node) {
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
        // Name of var or attribute
        Attribute attribute = node.getAttribute();
        // Name of owner
        Expression owner = ((WithExpr) node.getElementChoice()).getExpression();

        if (owner instanceof LocalIdentifier localIdentifier
                && attribute instanceof AttributeFromVariable varAttribute) {
            DataExpr e = varAttribute.getVariable();
            Qualified q = new Qualified(localIdentifier, e);
            uses.add(new Variable(q));
        } else if (owner instanceof Qualified && attribute instanceof AttributeFromVariable) {
            owner.accept(this);
        } else {
            attribute.accept(this);
        }

        node.getElementChoice().accept(this);

        if (node.getMetadata() != null) {
            ((ASTNode) node.getMetadata()).accept(this);
        }
    }

    @Override
    public void visit(Qualified node) {
        if (!(node.getSecond() instanceof ScratchList)) {
            uses.add(new Variable(node));
        }
    }

    @Override
    public void visit(de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable node) {
        if (actor != null) {
            Qualified q = new Qualified(actor.getIdent(), node);
            uses.add(new Variable(q));
        } else {
            uses.add(new Variable(node.getName()));
        }
    }

    @Override
    public void visit(Parameter node) {
        // Parameters are not variables
    }

    @Override
    public void visit(Answer node) {
        uses.add(new Variable(new StrId("answer")));
    }

    @Override
    public void visit(ProcedureDefinition node) {
        // Do not visit children
    }
}
