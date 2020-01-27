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
 * Checks for duplicated scripts. Only uses full String representation comparison.
 */
public class DuplicatedScript implements IssueFinder {

    String name = "duplicated_script";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
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

        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }
/*
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

 */

    @Override
    public String getName() {
        return name;
    }
}
