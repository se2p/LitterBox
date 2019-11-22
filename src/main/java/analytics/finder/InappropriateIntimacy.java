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
import java.util.List;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Checks for multiple access of private Sprite variables.
 */
public class InappropriateIntimacy implements IssueFinder {

    String name = "inappropriate_intimacy";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            List<String> counter = new ArrayList<>();
            for (Script script : scable.getScripts()) {
                if (project.getVersion().equals(Version.SCRATCH2)) {
                    searchBlocks(script.getBlocks(), counter, Identifier.LEGACY_SENSE.getValue());
                } else if (project.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks(script.getBlocks(), counter, Identifier.SENSE.getValue());
                }
            }
            if (counter.size() >= 4) {
                pos.add(scable.getName());
            }
        }
        count = pos.size();
        String notes = "There are no inappropriate intimacy issues in your project.";
        if (count > 0) {
            notes = "One ore more Sprites are excessively reading other sprite’s private variables (at least 4).";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }


    private void searchBlocks(List<ScBlock> blocks, List<String> count, String idf) {
        for (ScBlock b : blocks) {
            if (b.getCondition() != null) {
                if (b.getCondition().contains(idf)) {
                    count.add(b.toString());
                }
            } else if (b.getContent().contains(idf)) {
                count.add(b.toString());
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(b.getNestedBlocks(), count, idf);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(b.getElseBlocks(), count, idf);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
