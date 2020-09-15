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

import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;

import java.util.LinkedHashSet;
import java.util.Set;

public class VariableDefinitionVisitor implements DefinableCollector<Variable> {

    private Set<Variable> definitions = new LinkedHashSet<>();

    public Set<Variable> getDefineables() {
        return definitions;
    }

    @Override
    public void visit(Stmt node) {
        // Nop
    }

    @Override
    public void visit(AttributeAboveValue node) {
        // Nop
    }

    @Override
    public void visit(SetVariableTo node) {
        // Only the variable is a def
        node.getIdentifier().accept(this);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        // Only the variable is a def
        node.getIdentifier().accept(this);
    }

    @Override
    public void visit(Qualified node) {
        definitions.add(new Variable(node));
    }
}
