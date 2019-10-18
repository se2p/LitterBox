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
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if the project has a starting point / 'GreenFlag'.
 */
public class GlobalStartingPoint implements IssueFinder {

    private String note1 = "The project is correctly initialized and has a 'Green Flag'.";
    private String note2 = "The project is not correctly initialized and has no 'Green Flag'!";
    private String name = "has_global_start";

    @Override
    public IssueReport check(Project project) {
        if (project.getVersion().equals(Version.SCRATCH2)) {
            return runCheck(project, Identifier.LEGACY_GREEN_FLAG.getValue());
        } else if (project.getVersion().equals(Version.SCRATCH3)) {
            return runCheck(project, Identifier.GREEN_FLAG.getValue());
        }
        return null;
    }

    private IssueReport runCheck(Project project, String idf) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        boolean hasGreenFlag = false;
        int count = 0;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(idf)) {
                    hasGreenFlag = true;
                    break;
                }
            }
            if (hasGreenFlag) {
                break;
            }
        }
        String notes;
        if (!hasGreenFlag) {
            count = 1;
            //System.out.println(pos);
            pos = new ArrayList<>();
            pos.add("Project");
            notes = note2;
        } else {
            notes = note1;
        }
        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    @Override
    public String getName() {
        return name;
    }
}
