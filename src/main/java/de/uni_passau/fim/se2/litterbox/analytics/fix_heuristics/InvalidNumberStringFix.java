/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InvalidNumberString;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.regex.Pattern;

/**
 * Fix heuristic for the InvalidNumberString bug.
 *
 * <p>The bug is reported when a string that looks like a number but is invalid
 * is used in a numeric context.
 * The bug is fixed if the string is now a valid number or no longer looks like one.
 */
public class InvalidNumberStringFix extends AbstractFixHeuristic {
    private static final Pattern ALLOWED_CHARS = Pattern.compile("^[0-9eE.+\\-]+$");
    private static final Pattern VALID_NUMBER =
            Pattern.compile("^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$");

    private final IssueFinder relatedBugFinder = new InvalidNumberString();

    public InvalidNumberStringFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(AsNumber node) {
        if (matchesBugLocation(node)) {
            boolean fixed = true;
            if (node.getOperand1() instanceof StringLiteral stringLiteral) {
                String text = stringLiteral.getText();
                // Bug: matches ALLOWED_CHARS but NOT VALID_NUMBER
                // Fixed: !ALLOWED_CHARS OR VALID_NUMBER
                if (ALLOWED_CHARS.matcher(text).matches()
                        && !VALID_NUMBER.matcher(text).matches()) {
                    fixed = false;
                }
            }
            if (fixed) {
                addIssue(node, node.getMetadata());
            }
        }
        super.visit(node);
    }
}
