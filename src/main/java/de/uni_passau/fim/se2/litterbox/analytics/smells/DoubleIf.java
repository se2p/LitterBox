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
 * Checks for multiple ifs in a row with the same condition.
 */
public class DoubleIf implements IssueFinder {

    String name = "double_condition";

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
                    searchBlocks(scable, script, script.getBlocks(), pos);
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks3(scable, script, script.getBlocks(), pos);
                }
            }
        }
        count = pos.size();
        String notes = "There are no if clauses with the same condition twice in a row.";
        if (count > 0) {
            notes = "Some scripts have if clauses with the same condition twice in a row.";
        }

        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }
/*
    private void searchBlocks3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (b.getContent().startsWith(Identifier.IF.getValue())) {
                    String content2 = b.getCondition();
                    if (content1.equals(content2)) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                        continue;
                    }
                }
            }
            if (b.getContent().startsWith(Identifier.IF.getValue())) {
                content1 = b.getCondition();
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks3(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks3(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }
    *
 */

    @Override
    public String getName() {
        return name;
    }
}
