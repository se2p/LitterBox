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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

/**
 * Checks if the project has loose blocks without a head.
 */
public class DeadCode extends AbstractIssueFinder {

    public static final String NAME = "dead_code";
    public static final String SHORT_NAME = "dcode";
    public static final String HINT_TEXT = "Unused blocks found";

    @Override
    public void visit(Script node) {
        currentScript = node;
        if (node.getEvent() instanceof Never && node.getStmtList().getStmts().size() > 0) {
            // TODO: Replace with proper issue (including script etc)
            issues.add(new Issue(this, currentActor, node));
        }
        currentScript = null;
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }
}
