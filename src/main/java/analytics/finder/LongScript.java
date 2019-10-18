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

import analytics.IssueReport;
import analytics.IssueFinder;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Scriptable;
import scratch.structure.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks for scripts with more than 12 blocks.
 */
public class LongScript implements IssueFinder {

    String name = "long_script";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                int localCount = searchBlocks(script.getBlocks(), 0);
                if (localCount >= 12) {
                    pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                }
            }
        }
        count = pos.size();
        String notes = "There are no long scripts.";
        if (count > 0) {
            notes = "Some scripts are very long.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }


    private int searchBlocks(List<ScBlock> blocks, int count) {
        for (ScBlock b : blocks) {
            count = count + 1;
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                count = searchBlocks(b.getNestedBlocks(), count);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                count = searchBlocks(b.getElseBlocks(), count);
            }
        }
        return count;
    }

    @Override
    public String getName() {
        return name;
    }
}
