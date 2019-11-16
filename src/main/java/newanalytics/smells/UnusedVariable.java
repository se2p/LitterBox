package newanalytics.smells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.ScVariable;
import scratch.data.Script;
import scratch.newast.model.Program;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

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
