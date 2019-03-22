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
 * Checks for scripts with more than 12 blocks.
 */
public class LongScript implements IssueFinder {

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
                        int localCount = searchBlocks(script.getBlocks(), 0);
                        if (localCount > 12) {
                            pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                        }
                    }
                }
            }
        }
        count = pos.size();
        String notes = "There are no long scripts.";
        if (count > 0) {
            notes = "Some scripts are very long.";
        }

        String name = "long_script";
        return new Issue(name, count, pos, project.getPath(), notes);
    }

    private int searchBlocks(List<ScBlock> blocks, int count) {
        for (ScBlock b : blocks) {
            count = count + 1;
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                count = searchBlocks(b.getNestedBlocks(), count);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                count = searchBlocks(b.getElseBlocks(), count);
            }
        }
        return count;
    }


}
