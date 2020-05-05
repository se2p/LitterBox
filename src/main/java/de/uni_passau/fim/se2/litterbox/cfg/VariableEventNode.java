/*
 * Copyright (C) 2019 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.event.VariableAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;

public class VariableEventNode extends CFGNode {

    private Identifier variable;

    private NumExpr expression;

    private VariableAboveValue node;

    public VariableEventNode(VariableAboveValue node) {
        this.node = node;
        this.variable = node.getIdentifier();
        this.expression = node.getValue();
    }

    @Override
    public String toString() {
        // TODO: Actual variable name?
        return "Variable above value: "+variable.toString();
    }

    @Override
    public ASTNode getASTNode() {
        return node;
    }

    // TODO: Need equals/hashcode to support multiple events for the same variable+expression?
}
