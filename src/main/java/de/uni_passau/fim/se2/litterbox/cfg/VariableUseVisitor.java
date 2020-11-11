/*
 * Copyright (C) 2020 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;

import java.util.LinkedHashSet;
import java.util.Set;

public class VariableUseVisitor implements DefinableCollector<de.uni_passau.fim.se2.litterbox.cfg.Variable> {

    private Set<de.uni_passau.fim.se2.litterbox.cfg.Variable> uses = new LinkedHashSet<>();

    @Override
    public Set<de.uni_passau.fim.se2.litterbox.cfg.Variable> getDefineables() {
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
        // TODO: Handle this properly

        // Name of var or attribute
        Attribute attribute = node.getAttribute();
        // Name of owner
        Expression owner = ((WithExpr) node.getElementChoice()).getExpression();

        // Can only handle LocalIdentifier hier (i.e. value selected in dropdown)
        // We lose precision here because it could also be a Parameter or else
        // but we don't know the value of that statically
        if (owner instanceof LocalIdentifier) {
            LocalIdentifier localIdentifier = (LocalIdentifier) owner;

            if (attribute instanceof AttributeFromVariable) {
                AttributeFromVariable varAttribute = (AttributeFromVariable) attribute;
                DataExpr e = varAttribute.getVariable();
                Qualified q = new Qualified(localIdentifier, e);
                uses.add(new de.uni_passau.fim.se2.litterbox.cfg.Variable(q));
            }
        }
    }

    @Override
    public void visit(Qualified node) {
        if (!(node.getSecond() instanceof ScratchList)) {
            uses.add(new de.uni_passau.fim.se2.litterbox.cfg.Variable(node));
        }
    }
}
