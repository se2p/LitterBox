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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;

import java.util.LinkedHashSet;
import java.util.Set;

public class ListUseVisitor implements DefinableCollector<ListVariable> {

    private Set<ListVariable> defineables = new LinkedHashSet<>();

    @Override
    public Set<ListVariable> getDefineables() {
        return defineables;
    }

    @Override
    public void visit(AddTo stmt) {
        defineables.add(new ListVariable(stmt.getIdentifier()));
    }

    @Override
    public void visit(DeleteOf stmt) {
        defineables.add(new ListVariable(stmt.getIdentifier()));
        stmt.getNum().accept(this);
    }

    @Override
    public void visit(DeleteAllOf stmt) {
        // No use
    }

    @Override
    public void visit(HideList stmt) {
        // No use
    }

    @Override
    public void visit(ShowList stmt) {
        // No use
    }

    @Override
    public void visit(InsertAt stmt) {
        defineables.add(new ListVariable(stmt.getIdentifier()));
        stmt.getIndex().accept(this);
    }

    @Override
    public void visit(ReplaceItem stmt) {
        defineables.add(new ListVariable(stmt.getIdentifier()));
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
        if (node.getSecond() instanceof ScratchList) {
            defineables.add(new ListVariable(node));
        }
    }
}
