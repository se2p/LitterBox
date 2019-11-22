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
package newanalytics.smells;

import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.Program;
import scratch.data.ScBlock;

/**
 * Counts the blocks of a project.
 */
public class CountBlocks implements IssueFinder {

    String name = "block_count";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        int count = 0;
        List<Integer> countList = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                searchBlocks(script.getBlocks(), countList);
            }
        }
        for (int x : countList) {
            count += x;
        }
        return new IssueReport(name, count, new ArrayList<>(), program.getPath(), "");
         */
        throw new RuntimeException("not implemented");
    }

    private void searchBlocks(List<ScBlock> blocks, List<Integer> countList) {
        countList.add(blocks.size());
        for (ScBlock b : blocks) {
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(b.getNestedBlocks(), countList);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(b.getElseBlocks(), countList);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
