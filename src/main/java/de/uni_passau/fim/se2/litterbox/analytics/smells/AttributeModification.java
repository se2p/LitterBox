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
 * Checks if attributes get modified multiple times in a row.
 */
public class AttributeModification implements IssueFinder {

    String name = "multiple_attribute_modification";

    @Override
    public IssueReport check(Program program) {
        return runCheck(program);
    }

    private IssueReport runCheck(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (program.getVersion().equals(Version.SCRATCH2)) {
                    searchVariableModification(scable, script, script.getBlocks(), pos);
                    searchXYModification(scable, script, script.getBlocks(), pos);
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    searchVariableModification3(scable, script, script.getBlocks(), pos);
                    searchXYModification3(scable, script, script.getBlocks(), pos);
                }
            }
        }
        count = pos.size();
        String notes = "No variable gets modified multiple times in a row.";
        if (count > 0) {
            notes = "Some scripts modify the same variable multiple times in a row.";
        }

        return new IssueReport(name, count, pos, program.getPath(), notes);
         */
        throw new RuntimeException("not implemented");
    }

    /*
    private void searchVariableModification3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (b.getContent().startsWith(Identifier.CHANGE_VAR.getValue())) {
                    String content2 = b.getFields().get(Identifier.FIELD_VARIABLE.getValue()).get(0);
                    if (content1.equals(content2)) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                        break;
                    }
                }
            }
            if (b.getContent().equals(Identifier.CHANGE_VAR.getValue())) {
                content1 = b.getFields().get(Identifier.FIELD_VARIABLE.getValue()).get(0);

                if (content1 == null) {
                    content1 = "";
                }
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchVariableModification3(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchVariableModification3(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }

    private void searchXYModification3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (b.getContent().startsWith(Identifier.CHANGE_X.getValue()) && content1.equals("x") ||
                    b.getContent().startsWith(Identifier.CHANGE_Y.getValue()) && content1.equals("y")) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    break;
                }
            }
            if (b.getContent().startsWith(Identifier.CHANGE_X.getValue())) {
                content1 = "x";
                continue;
            } else if (b.getContent().startsWith(Identifier.CHANGE_Y.getValue())) {
                content1 = "y";
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchXYModification3(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchXYModification3(scable, sc, b.getElseBlocks(), pos);
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
