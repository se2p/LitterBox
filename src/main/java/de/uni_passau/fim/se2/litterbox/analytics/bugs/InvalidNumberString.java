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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.util.NumberUtil;

/**
 * Reports a likely error if a block takes a string as parameter (e.g. walk (x) steps) and that string looks like a
 * number but isn't. Specifically, it could be (1) a number with more than 1 decimal points, or (2) floating point
 * notation with "e".
 */
public class InvalidNumberString extends AbstractIssueFinder {

    public static final String NAME = "invalid_number_string";

    @Override
    public void visit(AsNumber node) {
        if (node.getOperand1() instanceof StringLiteral stringLiteral) {
            String text = stringLiteral.getText();
            // If it looks like a number (only contains number-like chars) but isn't a valid number
            if (NumberUtil.isInvalidNumberString(text)) {
                // Find the nearest ancestor with a block ID for proper fix heuristic matching
                ASTNode nodeWithBlockId = AstNodeUtil.findNearestNodeWithBlockId(node);
                if (nodeWithBlockId != null) {
                    addIssue(nodeWithBlockId, nodeWithBlockId.getMetadata(), IssueSeverity.LOW);
                } else {
                    // Fallback to the original node if no parent with block ID found
                    addIssue(node, node.getMetadata(), IssueSeverity.LOW);
                }
            }
        }
        // Continue traversal to find nested AsNumber nodes
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
