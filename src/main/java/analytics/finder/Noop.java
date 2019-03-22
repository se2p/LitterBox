package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch2.data.ScBlock;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Scriptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks if there are unused custom blocks in the project.
 */
public class Noop implements IssueFinder {

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count = 0;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        String x = "procDef";
                        if (script.getBlocks().size() > 0 && script.getBlocks().get(0).getContent().replace("\"", "").startsWith(x)) {
                            List<String> tempPos = new ArrayList<>();
                            String methodName = script.getBlocks().get(0).getContent().replace("\"procDef\"\"", "");
                            methodName = methodName.split("\"")[0];
                            for (Script script2 : scable.getScripts()) {
                                searchBlocks(script2.getBlocks(), scable, script2, tempPos, methodName);
                            }
                            if (tempPos.size() == 0) {
                                pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                            }
                        }
                    }
                }
            }
        }

        count = pos.size();
        String notes = "There are no unused custom blocks in your project.";
        if (count > 0) {
            notes = "There are unused custom blocks in your project.";
        }

        String name = "no_op";
        return new Issue(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks(List<ScBlock> blocks, Scriptable scable, Script script, List<String> tempPos, String methodName) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getContent().replace("\"", "").startsWith("call")) {
                    tempPos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                }
                if (block.getNestedBlocks() != null && block.getNestedBlocks().size() > 0) {
                    searchBlocks(block.getNestedBlocks(), scable, script, tempPos, methodName);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks(block.getElseBlocks(), scable, script, tempPos, methodName);
                }
            }
        }
    }

}
