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
 * Checks for middleman broadcasts.
 */
public class MiddleMan implements IssueFinder {

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
                        String x = "whenIReceive";
                        if (script.getBlocks().size() > 0 && script.getBlocks().get(0).getContent().replace("\"", "").startsWith(x)) {
                            searchBlocks(script.getBlocks(), scable, script, pos);
                        }
                    }
                }
            }
        }

        count = pos.size();
        String notes = "There is no Receive Script that broadcasts an event.";
        if (count > 0) {
            notes = "There is a Receive Script that broadcasts an event.";
        }

        String name = "middle_man";
        return new Issue(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks(List<ScBlock> blocks, Scriptable scable, Script script, List<String> pos) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getContent().replace("\"", "").startsWith("broadcast:") || block.getContent().replace("\"", "").startsWith("doBroadcastAndWait")) {
                    pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                }
                if (block.getNestedBlocks() != null && block.getNestedBlocks().size() > 0) {
                    searchBlocks(block.getNestedBlocks(), scable, script, pos);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks(block.getElseBlocks(), scable, script, pos);
                }
            }
        }
    }

}
