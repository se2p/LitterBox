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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;

public class UnnecessarySizeChange extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_size_change";
    private static final int MAX_SIZE = 540;

    @Override
    public void visit(SetSizeTo node) {
        NumExpr expr = node.getPercent();
        if (expr instanceof NumberLiteral numberLiteral) {
            double value = numberLiteral.getValue();
            if (value <= 0 || value >= MAX_SIZE) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW);
            }
        }
    }

    @Override
    public void visit(ChangeSizeBy node) {
        NumExpr expr = node.getNum();
        if (expr instanceof NumberLiteral numberLiteral) {
            double value = numberLiteral.getValue();
            if (value == 0 || value >= MAX_SIZE || value <= -MAX_SIZE) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW);
            }
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
