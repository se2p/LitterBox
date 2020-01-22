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

/**
 * Checks for race conditions.
 */
public class RaceCondition implements IssueFinder {

    String name = "race_condition";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        List<String> pos = new ArrayList<>();
        List<String> variables = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                List<String> temp = new ArrayList<>();
                if (program.getVersion().equals(Version.SCRATCH2)) {
                    checkVariables2(script, temp);
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    checkVariables3(script, temp);
                }
                for (String s : temp) {
                    if (variables.contains(s)) {
                        pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                    } else {
                        variables.add(s);
                    }
                }
            }
        }
        String note = "No variable gets initialised multiple times from different scripts at the beginning.";
        if (pos.size() > 0) {
            note = "Some variables get initialised multiple times from different scripts at the beginning.";

        }
        return new IssueReport(name, pos.size(), pos, program.getPath(), note);

         */
        throw new RuntimeException("not implemented");
    }
/*
    private void checkVariables3(Script script, List<String> temp) {
        if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(Identifier.GREEN_FLAG.getValue())) {
            for (ScBlock b : script.getBlocks()) {
                if (b.getContent().startsWith(Identifier.SET_VAR.getValue())) {
                    String var = b.getFields().get(Identifier.FIELD_VARIABLE.getValue()).get(0);
                    temp.add(var);
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
