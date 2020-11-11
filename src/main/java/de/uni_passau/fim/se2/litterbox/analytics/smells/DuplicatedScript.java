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

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicatedScript extends TopBlockFinder {

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
        Set<Script> checked = new HashSet<>();
        List<Script> scripts = node.getScriptList();
        for (Script s : scripts) {
            if (ignoreLooseBlocks && s.getEvent() instanceof Never) {
                // Ignore unconnected blocks
                return;
            }
            setHint = false;
            currentScript = s;

            for (Script other : scripts) {
                if (s == other || checked.contains(other)) {
                    continue;
                }

                if (s.equals(other)) {
                    checked.add(s);
                    setHint = true;
                    if (!(s.getEvent() instanceof Never)) {
                        Event event = s.getEvent();
                        addIssue(event, event.getMetadata());
                    } else {
                        s.getStmtList().accept(this);
                    }

                    break;
                }
            }
        }
    }
}
