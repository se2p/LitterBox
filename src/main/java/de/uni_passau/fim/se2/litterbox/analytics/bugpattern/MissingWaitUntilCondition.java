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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;

/**
 * Checks for missing statements in repeat-until blocks.
 */
public class MissingWaitUntilCondition extends AbstractIssueFinder {

    public static final String NAME = "missing_wait_condition";
    public static final String SHORT_NAME = "mssWaitCond";
    public static final String HINT_TEXT = "missing wait condition";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(WaitUntil node) {
        if (node.getUntil() instanceof UnspecifiedBoolExpr) {
            issues.add(new Issue(this, currentActor, node,
                    HINT_TEXT, node.getMetadata()));
        }
    }

    @Override
    public void visit(Script node) {
        currentScript = node;
        if (!(node.getEvent() instanceof Never)) {
            visitChildren(node);
        }
    }
}
