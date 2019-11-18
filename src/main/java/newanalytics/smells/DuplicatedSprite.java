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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.newast.model.Program;

/**
 * Checks for duplicated sprites.
 */
public class DuplicatedSprite implements IssueFinder {

    String name = "duplicated_sprite";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        int count;
        List<String> duplicated = new ArrayList<>();
        List<String> pos = new ArrayList<>();
        Map<String, List<String>> scriptMap = new HashMap<>();
        for (Scriptable scable : scriptables) {
            if (scable.getScripts().size() > 0) {
                scriptMap.put(scable.getName(), new ArrayList<>());
                for (Script script : scable.getScripts()) {
                    scriptMap.get(scable.getName()).add(script.getBlocks().toString());
                }
            }
        }
        for (String check : scriptMap.keySet()) {
            for (String check2 : scriptMap.keySet()) {
                if (!check.equals(check2)) {
                    if (equalLists(scriptMap.get(check), scriptMap.get(check2))) {
                        if (!pos.contains(check2 + " and " + check) && !duplicated.contains(check2)) {
                            pos.add(check + " and " + check2);
                            duplicated.add(check2);
                        }
                    }
                }
            }
        }
        count = pos.size();
        String notes = "There are no duplicated sprites in your project.";
        if (count > 0) {
            notes = "There are duplicated sprites in your project.";
        }

        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }

    private boolean equalLists(List<String> one, List<String> two) {
        if (one == null && two == null) {
            return true;
        }

        if (one == null || two == null || one.size() != two.size()) {
            return false;
        }

        one = new ArrayList<>(one);
        two = new ArrayList<>(two);
        Collections.sort(one);
        Collections.sort(two);
        return one.equals(two);
    }

    @Override
    public String getName() {
        return name;
    }
}
