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
 * Checks for nested loops.
 */
public class NestedLoops implements IssueFinder {

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
                        searchBlocks(scable, script, script.getBlocks(), pos);
                    }
                }
            }
        }
        count = pos.size();
        String notes = "There are no nested loops in your scripts.";
        if (count > 0) {
            notes = "Some scripts have nested loops.";
        }

        String name = "nested_loops";
        return new Issue(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().replace("\"", "").startsWith("doForever") || b.getContent().replace("\"", "").startsWith("doForeverIf")
                    || b.getContent().replace("\"", "").startsWith("doForLoop") || b.getContent().replace("\"", "").startsWith("doUntil")) {
                if (b.getNestedBlocks() != null && b.getNestedBlocks().size() == 1) {
                    ScBlock nested = b.getNestedBlocks().get(0);
                    if (nested.getContent().replace("\"", "").startsWith("doForever") || nested.getContent().replace("\"", "").startsWith("doForeverIf")
                            || nested.getContent().replace("\"", "").startsWith("doForLoop") || nested.getContent().replace("\"", "").startsWith("doUntil")) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    }
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }
}