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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ComparingLiterals;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the ComparingLiterals bug.
 *
 * <p>The bug is reported when a comparison operator compares two literals
 * (string or number), which always evaluates to the same result.
 *
 * <p>The bug is fixed if at least one operand is no longer a literal.
 */
public class ComparingLiteralsFix extends AbstractFixHeuristic {

    private final ComparingLiterals relatedBugFinder = new ComparingLiterals();

    public ComparingLiteralsFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(BiggerThan node) {
        checkComparisonFix(node, node.getOperand1(), node.getOperand2());
        visitChildren(node);
    }

    @Override
    public void visit(LessThan node) {
        checkComparisonFix(node, node.getOperand1(), node.getOperand2());
        visitChildren(node);
    }

    @Override
    public void visit(Equals node) {
        checkComparisonFix(node, node.getOperand1(), node.getOperand2());
        visitChildren(node);
    }

    /**
     * Check if a comparison operation has been fixed.
     *
     * @param node the comparison node
     * @param op1  first operand
     * @param op2  second operand
     */
    private void checkComparisonFix(ASTNode node, ComparableExpr op1, ComparableExpr op2) {
        if (matchesBugLocation(node) && isNotStatic(op1, op2)) {
            addFix(node, node.getMetadata());
        }
    }

    /**
     * Returns true if at least one operand is NOT a literal (meaning the bug is fixed).
     */
    private boolean isNotStatic(ComparableExpr op1, ComparableExpr op2) {
        return !(isComparableLiteral(op1) && isComparableLiteral(op2));
    }

    /**
     * Checks if the expression is a literal constant (not a variable or computed value).
     * Only StringLiteral and NumberLiteral need to be checked because BoolLiteral and
     * ColorLiteral cannot appear in comparison blocks (they don't implement ComparableExpr).
     */
    private boolean isComparableLiteral(ComparableExpr expr) {
        return expr instanceof StringLiteral || expr instanceof NumberLiteral;
    }
}
