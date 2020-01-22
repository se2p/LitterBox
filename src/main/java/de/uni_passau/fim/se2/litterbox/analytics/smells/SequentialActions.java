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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

/**
 * Checks for sequential actions that can be replaced by (do x times).
 */
public class SequentialActions implements IssueFinder {

    String name = "sequential_actions";

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
                if (script.getBlocks().size() > 1) {
                    searchVariableModification(scable, script, script.getBlocks(), pos, program);
                }
            }
        }
        count = pos.size();
        String notes = "There are no sequential actions with the same content in your project.";
        if (count > 0) {
            notes = "Some scripts have sequential actions with the same content.";
        }

        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }
/*
    private void searchVariableModification(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos, Project project) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (b.getContent().equals(content1)) {
                pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                    searchVariableModification(scable, sc, b.getNestedBlocks(), pos, project);
                }
                if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                    searchVariableModification(scable, sc, b.getElseBlocks(), pos, project);
                }
                break;
            }
            String toSearch = "";
            if (project.getVersion().equals(Version.SCRATCH2)) {
                toSearch = Identifier.LEGACY_WAIT.getValue();
            } else if (project.getVersion().equals(Version.SCRATCH3)) {
                toSearch = Identifier.WAIT.getValue();
            }
            if (b.getContent().startsWith(toSearch)) {
                continue;
            } else {
                content1 = b.getContent();
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchVariableModification(scable, sc, b.getNestedBlocks(), pos, project);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchVariableModification(scable, sc, b.getElseBlocks(), pos, project);
            }
        }
    }

 */

    @Override
    public String getName() {
        return name;
    }
}
