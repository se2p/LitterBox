package analytics.finder;

import analytics.Issue;
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
 * Checks if there are unused variables.
 */
public class UnusedVariable implements IssueFinder {

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>(project.getSprites());
        scriptables.add(project.getStage());
        int count;
        List<String> pos = new ArrayList<>();
        Map<String, List<String>> variableScope = new HashMap<>();
        List<ScVariable> vars = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            vars.addAll(scable.getVariables());
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        if (project.getVersion().equals(Version.SCRATCH2)) {
                            searchBlocks2(script.getBlocks(), scable, variableScope);
                        } else if (project.getVersion().equals(Version.SCRATCH3)) {
                            searchBlocks3(script.getBlocks(), scable, variableScope);
                        }
                    }
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

        String name = "unused_variable";
        return new Issue(name, count, pos, project.getPath(), notes);
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

}
