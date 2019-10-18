/**
 * Copyright (C) 2019 LitterBox contributors
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
package analytics.finder;

import analytics.IssueReport;
import analytics.IssueFinder;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Scriptable;
import scratch.structure.Project;
import utils.Identifier;
import utils.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks for empty if or else bodies.
 */
public class EmptyBody implements IssueFinder {

    String name = "empty_body";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (project.getVersion().equals(Version.SCRATCH2)) {
                    searchBlocks(scable, script, script.getBlocks(), pos, Identifier.LEGACY_IF.getValue(), Identifier.LEGACY_IF_ELSE.getValue());
                } else if (project.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks(scable, script, script.getBlocks(), pos, Identifier.IF.getValue(), Identifier.IF_ELSE.getValue());
                }
            }
        }
        count = pos.size();
        String notes = "All 'if' blocks have a body.";
        if (count > 0) {
            notes = "Some 'if' blocks have no body.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }


    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos, String ifId, String ifElseId) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(ifElseId)) {
                if (b.getNestedBlocks() == null || b.getNestedBlocks().size() == 0) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
            } else {
                if (b.getContent().startsWith(ifId)) {
                    if (b.getNestedBlocks() == null || b.getNestedBlocks().size() == 0) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    }
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getNestedBlocks(), pos, ifId, ifElseId);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getElseBlocks(), pos, ifId, ifElseId);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
