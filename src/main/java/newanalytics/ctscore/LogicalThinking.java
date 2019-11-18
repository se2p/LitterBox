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
package newanalytics.ctscore;

import java.util.Arrays;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.newast.model.Program;
import scratch.structure.Scriptable;

/**
 * Evaluates the logical thinking level of the program.
 */
public class LogicalThinking implements IssueFinder {

    private String[] notes = new String[4];
    private String name = "logical_thinking";

    public LogicalThinking() {

        notes[0] = "There are no if conditions.";
        notes[1] = "Basic Level. There are no if else conditions.";
        notes[2] = "Developing Level. There are no logic operations.";
        notes[3] = "Proficiency Level. Good work!";
    }

    /**
     * {@inheritDoc}
     * @param program
     */
    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        List<String> pos = new ArrayList<>();
        List<String> found = new ArrayList<>();
        int level = 0;

        List<List<String>> versionIds = checkVersion(program);

        for (int i = 0; i < versionIds.size(); i++) {
            for (Scriptable scable : scriptables) {
                for (Script script : scable.getScripts()) {
                    search(scable, script, script.getBlocks(), found,
                            versionIds.get(i));
                }
            }
            if (found.size() > 0) {
                level = i + 1;
                pos.addAll(found);
                found.clear();
            }
        }

        return new IssueReport(name, level, pos, program.getPath(),
                notes[level]);
         */
        throw new RuntimeException("not implemented");
    }

    /**
     * Searches the scripts of the project for the given identifiers.
     *
     * @param scable Scriptable objects.
     * @param sc     Scripts in the project.
     * @param blocks All blocks that are given in the scripts.
     * @param found  The identifiers that were found.
     * @param ids    The identifiers for the current version of the project.
     */
    private void search(Scriptable scable, Script sc,
        List<ScBlock> blocks, List<String> found,
        List<String> ids) {
        for (ScBlock b : blocks) {
            if (ids.contains(b.getContent())) {
                if (found.size() < 10) {
                    found.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                search(scable, sc, b.getNestedBlocks(), found, ids);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                search(scable, sc, b.getElseBlocks(), found, ids);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
