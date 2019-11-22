/*
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

import analytics.IssueFinder;
import analytics.IssueReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Checks if there is cloning in the project and if all clones are initialized correctly.
 */
public class CloneInitialization implements IssueFinder {

    String name = "clone_initialization";

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
                    searchBlocks(scable, script, script.getBlocks(), pos);
                } else if (project.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks3(project, scable, script, script.getBlocks(), pos);
                }
            }
        }
        count = pos.size();
        String notes = "Cloning is done right in this project or does not exist.";
        if (count > 0) {
            notes = "Some scriptables were cloned but never initialized.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks3(Project project, Scriptable scr, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(Identifier.CREATE_CLONE.getValue())) {
                boolean check = false;
                Scriptable scable = null;
                if (b.getCreatedClone() == null) {
                    break;
                }
                if (Identifier.MYSELF.getValue().equals(b.getCreatedClone())) {
                    scable = scr;
                } else {
                    for (Scriptable scri : project.getSprites()) {
                        if (b.getCreatedClone() != null && b.getCreatedClone().equals(scri.getName())) {
                            scable = scri;
                            break;
                        }
                    }
                }
                if (scable == null) {
                    return;
                }
                for (Script s : scable.getScripts()) {
                    if (s.getBlocks().get(0).getContent().startsWith(Identifier.START_CLONE.getValue())) {
                        check = true;
                        break;
                    }
                }
                if (!check) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks3(project, scr, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks3(project, scr, sc, b.getElseBlocks(), pos);
            }
        }
    }

    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().replace("\"", "").startsWith(Identifier.LEGACY_START_CLONE.getValue())) {
                boolean check = false;
                for (Script s : scable.getScripts()) {
                    if (s.getBlocks().get(0).getContent().replace("\"", "").startsWith(Identifier.LEGACY_CREATE_CLONE.getValue())) {
                        check = true;
                        break;
                    }
                }
                if (!check) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }


    @Override
    public String getName() {
        return name;
    }

}
