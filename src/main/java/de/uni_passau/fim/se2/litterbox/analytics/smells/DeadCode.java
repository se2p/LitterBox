/*
 * Copyright (C) 2019-2021 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

/**
 * Checks if the project has loose blocks without a head.
 */
public class DeadCode extends TopBlockFinder {

    public static final String NAME = "dead_code";
    private boolean addHint = false;

    public DeadCode(){
        addExtensionVisitor(this);
    }

    @Override
    public void visit(Script node) {
        currentScript = node;
        if (node.getEvent() instanceof Never && node.getStmtList().getStmts().size() > 0) {
            addHint = true;
            node.getStmtList().getStmts().get(0).accept(this);
        }
        addHint = false;
        currentScript = null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}
