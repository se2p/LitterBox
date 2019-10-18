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
package analytics.finder;

import analytics.IssueReport;
import analytics.IssueFinder;
import scratch.data.ScBlock;
import scratch.data.ScVariable;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks if there are variables with a broad scope.
 */
public class VariableScope implements IssueFinder {

    String name = "variable_scope";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>(project.getSprites());
        scriptables.add(project.getStage());
        int count;
        List<String> pos = new ArrayList<>();
        Map<String, List<String>> variableScope = new HashMap<>();
        List<ScVariable> vars = project.getStage().getVariables();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script.getBlocks().size() > 1) {
                    if (project.getVersion().equals(Version.SCRATCH2)) {
                        searchBlocks2(script.getBlocks(), scable, variableScope);
                    } else if (project.getVersion().equals(Version.SCRATCH3)) {
                        searchBlocks3(script.getBlocks(), scable, variableScope);
                    }
                }
            }
        }
        for (ScVariable scv : vars) {
            if (variableScope.containsKey(scv.getName())) {
                if (variableScope.get(scv.getName()).size() == 1) {
                    if (!variableScope.get(scv.getName()).get(0).equals("Stage")) {
                        pos.add(scv.getName() + " in " + variableScope.get(scv.getName()).get(0));
                    }
                }
            }
        }

        count = pos.size();
        String notes = "There are no variables with a broad scope in your project.";
        if (count > 0) {
            notes = "There are global variables, that are only used in one single sprite.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
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

    private void searchBlocks2(List<ScBlock> blocks, Scriptable scable, Map<String, List<String>> variableScope) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getContent().replace("\"", "").contains(Identifier.LEGACY_READ_VAR.getValue())) {
                    String[] splits = block.getContent().split(",");
                    int count = 0;
                    for (String s : splits) {
                        if (s.contains(Identifier.LEGACY_READ_VAR.getValue())) {
                            try {
                                String split = splits[count + 1].replace("\"", "").split("]")[0];
                                if (variableScope.containsKey(split)) {
                                    if (!variableScope.get(split).contains(scable.getName())) {
                                        variableScope.get(split).add(scable.getName());
                                    }
                                } else {
                                    variableScope.put(split, new ArrayList<>());
                                    variableScope.get(split).add(scable.getName());
                                }
                            } catch (IndexOutOfBoundsException ex) {
                                continue;
                            }
                        }
                        count++;
                    }
                } else if (block.getContent().contains(Identifier.LEGACY_CHANGE_VAR.getValue()) ||
                        block.getContent().contains(Identifier.LEGACY_SETVAR.getValue())) {
                    String[] splits = block.getContent().replace(Identifier.LEGACY_SETVAR.getValue(), "")
                            .replace(Identifier.LEGACY_CHANGE_VAR.getValue(), "").split("\"");
                    try {
                        String split = splits[0];
                        if (variableScope.containsKey(split)) {
                            if (!variableScope.get(split).contains(scable.getName())) {
                                variableScope.get(split).add(scable.getName());
                            }
                        } else {
                            variableScope.put(split, new ArrayList<>());
                            variableScope.get(split).add(scable.getName());
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        continue;
                    }
                }
                if (block.getNestedBlocks() != null && block.getNestedBlocks().size() > 0) {
                    searchBlocks2(block.getNestedBlocks(), scable, variableScope);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks2(block.getElseBlocks(), scable, variableScope);
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
