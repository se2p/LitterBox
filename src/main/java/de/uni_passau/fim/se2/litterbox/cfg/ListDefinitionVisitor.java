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

import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class ListDefinitionVisitor implements DefinableCollector<ListVariable> {

    private Set<ListVariable> definitions = new LinkedHashSet<>();

    public Set<ListVariable> getDefineables() {
        return definitions;
    }

    @Override
    public void visit(AddTo stmt) {
        definitions.add(new ListVariable(stmt.getIdentifier()));
    }

    @Override
    public void visit(DeleteAllOf stmt) {
        definitions.add(new ListVariable(stmt.getIdentifier()));
    }

    @Override
    public void visit(DeleteOf stmt) {
        definitions.add(new ListVariable(stmt.getIdentifier()));
    }

    @Override
    public void visit(InsertAt stmt) {
        definitions.add(new ListVariable(stmt.getIdentifier()));
    }

    @Override
    public void visit(ReplaceItem stmt) {
        definitions.add(new ListVariable(stmt.getIdentifier()));
    }

    @Override
    public void visit(HideList stmt) {
        // Nop
    }

    @Override
    public void visit(ShowList stmt) {
        // Nop
    }

    @Override
    public void visit(Stmt node) {
        // Nop
    }
}
