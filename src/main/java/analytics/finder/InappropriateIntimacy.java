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
 * Checks for multiple access of private Sprite variables.
 */
public class InappropriateIntimacy implements IssueFinder {

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            List<String> counter = new ArrayList<>();
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        searchBlocks(script.getBlocks(), counter);
                    }
                }
            }
            if (counter.size() >= 4) {
                pos.add(scable.getName());
            }
        }
        count = pos.size();
        String notes = "There are no inappropriate intimacy issues in your project.";
        if (count > 0) {
            notes = "One ore more Sprites are excessively reading other sprite’s private variables (at least 4).";
        }

        String name = "inappropriate_intimacy";
        return new Issue(name, count, pos, project.getPath(), notes);
    }


    private void searchBlocks(List<ScBlock> blocks, List<String> count) {
        for (ScBlock b : blocks) {
            if (b.getContent().contains("getAttribute")) {
                count.add(b.toString());
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(b.getNestedBlocks(), count);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(b.getElseBlocks(), count);
            }
        }
    }

}
