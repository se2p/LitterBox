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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InvalidNumberString;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.util.NumberUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the InvalidNumberString bug.
 *
 * <p>The bug is reported when a string that looks like a number but is invalid
 * is used in a numeric context. The bug is reported on the parent node that has a block ID,
 * not directly on the AsNumber expression.
 *
 * <p>The bug is fixed if:
 * <ul>
 *   <li>The block with the bug location ID is deleted, OR</li>
 *   <li>The string is now a valid number, OR</li>
 *   <li>The string no longer looks like a number</li>
 * </ul>
 */
public class InvalidNumberStringFix extends AbstractFixHeuristic {

    private final InvalidNumberString relatedBugFinder = new InvalidNumberString();

    public InvalidNumberStringFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ASTNode node) {
        if (matchesBugLocation(node)) {
            // Found the node that matches the bug location
            // Check if all AsNumber children have valid numbers now
            InvalidNumberChecker checker = new InvalidNumberChecker();
            node.accept(checker);

            if (!checker.hasInvalidNumber) {
                // The bug is fixed - no invalid numbers found in this subtree
                addFix(node, node.getMetadata());
            }
        } else {
            visitChildren(node);
        }
    }

    /**
     * Inner visitor to check if any AsNumber in a subtree still has an invalid number string.
     * Prevents traversing into next statements or substacks to keep the check local.
     */
    private static final class InvalidNumberChecker implements ScratchVisitor {
        boolean hasInvalidNumber = false;

        @Override
        public void visit(ASTNode node) {
            if (node instanceof AsNumber asNumber) {
                if (asNumber.getOperand1() instanceof StringLiteral stringLiteral) {
                    String text = stringLiteral.getText();
                    if (NumberUtil.isInvalidNumberString(text)) {
                        hasInvalidNumber = true;
                    }
                }
            }
            if (!hasInvalidNumber) {
                visitChildren(node);
            }
        }

        @Override
        public void visitChildren(ASTNode node) {
            for (ASTNode child : node.getChildren()) {
                if (child == null) {
                    continue;
                }
                // Skip traversing into other statements (next blocks) or statement lists (substacks)
                // This ensures we only check the expressions (parameters) of the current block
                if (child instanceof StmtList) {
                    continue;
                }
                child.accept(this);
            }
        }
    }
}
