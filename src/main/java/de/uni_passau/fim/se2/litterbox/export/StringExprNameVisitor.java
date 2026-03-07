/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.export;

import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

/**
 * Simple visitor to extract sprite/touchable names from string expressions.
 *
 * <p>This visitor is used by export visitors to extract literal names from
 * expressions that represent sprite names, costume names, or other identifiers.
 */
public class StringExprNameVisitor implements ScratchVisitor {
    private String result = "";

    /**
     * Returns the extracted name result.
     *
     * @return The extracted name, or empty string if none found
     */
    public String getResult() {
        return result;
    }

    @Override
    public void visit(StringLiteral node) {
        result = node.getText();
    }

    @Override
    public void visit(Variable node) {
        result = node.getName().getName();
    }
}
