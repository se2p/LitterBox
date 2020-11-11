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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

/**
 * If two loops are nested and the inner loop is a forever loop, the inner loop will never terminate. Thus
 * the statements preceeding the inner loop are only executed once. Furthermore, the statements following the outer
 * loop can never be reached.
 */
public class ForeverInsideLoop extends AbstractIssueFinder {
    public static final String NAME = "forever_inside_loop";
    private int loopcounter;

    @Override
    public void visit(ActorDefinition actor) {
        loopcounter = 0;
        super.visit(actor);
    }

    @Override
    public void visit(UntilStmt node) {
        loopcounter++;
        visitChildren(node);
        loopcounter--;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (loopcounter > 0) {
            addIssue(node, node.getMetadata());
        }
        loopcounter++;
        visitChildren(node);
        loopcounter--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        loopcounter++;
        visitChildren(node);
        loopcounter--;
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
