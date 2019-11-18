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
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;

/**
 * Checks for duplicated scripts. Only uses full String representation comparison.
 */
public class DuplicatedScript implements IssueFinder {

    String name = "duplicated_script";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        List<String> duplicated = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script.getBlocks().size() > 1) {
                    searchBlocks(scriptables, scable, script, pos, duplicated);
                }
            }
        }
        count = pos.size();
        String notes = "No duplicated code found.";
        if (count > 0) {
            notes = "Some scripts have duplicated code.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks(List<Scriptable> scriptables, Scriptable currentSc, Script sc, List<String> pos, List<String> duplicated) {
        String toSearch = sc.getBlocks().toString();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script.getBlocks().size() > 1) {
                    if (script.getBlocks().toString().equals(toSearch) && script.getPosition() != sc.getPosition() && !duplicated.contains(toSearch)) {
                        pos.add(currentSc.getName() + " and " + scable.getName() + " at " + Arrays.toString(sc.getPosition()) + " and " + Arrays.toString(script.getPosition()));
                        duplicated.add(toSearch);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
