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
import java.util.List;
import java.util.Map;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.Program;
import scratch.data.ScBlock;
import scratch.structure.Scriptable;
import utils.Identifier;

/**
 * Checks if there are unused variables.
 */
public class UnusedVariable implements IssueFinder {

    String name = "unused_variable";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>(program.getSprites());
        scriptables.add(program.getStage());
        int count;
        List<String> pos = new ArrayList<>();
        Map<String, List<String>> variableScope = new HashMap<>();
        List<ScVariable> vars = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            vars.addAll(scable.getVariables());
            for (Script script : scable.getScripts()) {
                if (program.getVersion().equals(Version.SCRATCH2)) {
                    searchBlocks2(script.getBlocks(), scable, variableScope);
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks3(script.getBlocks(), scable, variableScope);
                }
            }
        }
        for (ScVariable scv : vars) {
            if (!variableScope.containsKey(scv.getName())) {
                pos.add(scv.getName());
            }
        }

        count = pos.size();
        String notes = "There are no unused variables in your project.";
        if (count > 0) {
            notes = "There are unused variables in your project.";
        }

        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }

    private void searchBlocks3(List<ScBlock> blocks, Scriptable scable, Map<String, List<String>> variableScope) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getFields() != null && block.getFields().containsKey(Identifier.FIELD_VARIABLE.getValue())) {
                    String var = block.getFields().get(Identifier.FIELD_VARIABLE.getValue()).get(0);
                    if (variableScope.containsKey(var)) {
                        if (!variableScope.get(var).contains(scable.getName())) {
                            variableScope.get(var).add(scable.getName());
                        }
                    } else {
                        variableScope.put(var, new ArrayList<>());
                        variableScope.get(var).add(scable.getName());
                    }
                }
                if (block.getNestedBlocks() != null && block.getNestedBlocks().size() > 0) {
                    searchBlocks3(block.getNestedBlocks(), scable, variableScope);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks3(block.getElseBlocks(), scable, variableScope);
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
