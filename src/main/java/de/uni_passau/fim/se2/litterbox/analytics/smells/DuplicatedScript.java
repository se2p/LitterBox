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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

import java.util.List;
import java.util.stream.Collectors;

public class DuplicatedScript extends AbstractIssueFinder {

    private static final String NAME = "duplicated_script";

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ScriptList node) {
        List<Script> scripts;
        if (ignoreLooseBlocks) {
            scripts = node.getScriptList().stream().filter(s -> !(s.getEvent() instanceof Never)).collect(Collectors.toList());
        } else {
            scripts = node.getScriptList();
        }
        for (int i = 0; i < scripts.size() - 1; i++) {
            currentScript = scripts.get(i);

            for (int j = i + 1; j < scripts.size(); j++) {
                Script script2 = scripts.get(j);

                //Todo: a change of equals/hash would break this
                if (currentScript.equals(script2)) {
                    ASTNode topBlockCurrent;
                    if (!(currentScript.getEvent() instanceof Never)) {
                        topBlockCurrent = currentScript.getEvent();
                    } else {
                        topBlockCurrent = currentScript.getStmtList().getStmts().get(0);
                    }
                    addIssue(topBlockCurrent, topBlockCurrent.getMetadata());
                }
            }
        }
    }

    @Override
    public boolean isSubsumedBy(Issue theIssue, Issue other) {
        if (theIssue.getFinder() != this) {
            return super.isSubsumedBy(theIssue, other);
        }

        if (other.getFinder() instanceof DuplicatedScriptsCovering) {
            return theIssue.getCodeLocation().equals(other.getCodeLocation());
        }
        return false;
    }
}
