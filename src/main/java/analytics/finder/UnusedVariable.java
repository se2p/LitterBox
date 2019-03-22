package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch2.data.ScBlock;
import scratch2.data.ScVariable;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Scriptable;

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
        int count = 0;
        List<String> pos = new ArrayList<>();
        Map<String, List<String>> variableScope = new HashMap<>();
        List<ScVariable> vars = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            vars.addAll(scable.getVariables());
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        searchBlocks(script.getBlocks(), scable, variableScope);
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

    private void searchBlocks(List<ScBlock> blocks, Scriptable scable, Map<String, List<String>> variableScope) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getContent().replace("\"", "").contains("readVariable")) {
                    String[] splits = block.getContent().split(",");
                    List<String> vars = new ArrayList<>();
                    int count = 0;
                    for (String s : splits) {
                        if (s.contains("readVariable")) {
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
                } else if (block.getContent().replace("\"", "").contains("changeVar") ||
                        block.getContent().replace("\"", "").contains("setVar")) {
                    String[] splits = block.getContent().replace("\"setVar:to:\"\"", "").replace("\"changeVar:by:\"\"", "").split("\"");
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
                    searchBlocks(block.getNestedBlocks(), scable, variableScope);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks(block.getElseBlocks(), scable, variableScope);
                }
            }
        }
    }

}
