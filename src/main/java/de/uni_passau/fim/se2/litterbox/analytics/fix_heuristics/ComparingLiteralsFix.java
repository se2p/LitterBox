/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

public class ComparingLiteralsFix extends AbstractIssueFinder {
    public static final String NAME = "comparing_literals_fix";
    private final String bugLocationBlockId;

    public ComparingLiteralsFix(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    @Override
    public void visit(BiggerThan node) {
        if (
                AstNodeUtil.hasBlockId(node, bugLocationBlockId)
                        && checkNotStatic(node.getOperand1(), node.getOperand2())
        ) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(LessThan node) {
        if (
                AstNodeUtil.hasBlockId(node, bugLocationBlockId)
                        && checkNotStatic(node.getOperand1(), node.getOperand2())
        ) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(Equals node) {
        if (
                AstNodeUtil.hasBlockId(node, bugLocationBlockId)
                        && checkNotStatic(node.getOperand1(), node.getOperand2())
        ) {
            addIssue(node, node.getMetadata());
        }
    }

    private boolean checkNotStatic(ComparableExpr operand1, ComparableExpr operand2) {
        return !((operand1 instanceof StringLiteral || operand1 instanceof NumberLiteral)
                && (operand2 instanceof StringLiteral || operand2 instanceof NumberLiteral));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.FIX;
    }
}
