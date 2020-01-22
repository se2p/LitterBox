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

import java.util.Arrays;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.Program;
import utils.Identifier;

/**
 * Checks for middleman broadcasts.
 */
public class MiddleMan implements IssueFinder {

    String name = "middle_man";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (program.getVersion().equals(Version.SCRATCH2)) {
                    if (script.getBlocks().size() > 0 && script.getBlocks().get(0).getContent().startsWith(Identifier.LEGACY_RECEIVE.getValue())) {
                        searchBlocks(script.getBlocks(), scable, script, pos);
                    }
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    if (script.getBlocks().size() > 0 && script.getBlocks().get(0).getContent().startsWith(Identifier.RECEIVE.getValue())) {
                        searchBlocks3(script.getBlocks(), scable, script, pos);
                    }
                }
            }
        }

        count = pos.size();
        String notes = "There is no Receive Script that broadcasts an event.";
        if (count > 0) {
            notes = "There is a Receive Script that broadcasts an event.";
        }
        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }
/*
    private void searchBlocks3(List<ScBlock> blocks, Scriptable scable, Script script, List<String> pos) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getContent().startsWith(Identifier.BROADCAST.getValue())
                    || block.getContent().startsWith(Identifier.BROADCAST_WAIT.getValue())) {
                    pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                }
                if (block.getNestedBlocks() != null && block.getNestedBlocks().size() > 0) {
                    searchBlocks3(block.getNestedBlocks(), scable, script, pos);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks3(block.getElseBlocks(), scable, script, pos);
                }
            }
        }
    }

 */

    @Override
    public String getName() {
        return name;
    }
}
