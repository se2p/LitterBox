package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Scriptable;
import scratch.structure.Project;
import utils.Identifier;
import utils.Version;

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
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        if (project.getVersion().equals(Version.SCRATCH2)) {
                            searchBlocks(scable, script, script.getBlocks(), pos);
                        } else if (project.getVersion().equals(Version.SCRATCH3)) {
                            searchBlocks3(scable, script, script.getBlocks(), pos);
                        }
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

    private void searchBlocks3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(Identifier.FOREVER.getValue())
                    || b.getContent().startsWith(Identifier.REPEAT.getValue())
                    || b.getContent().startsWith(Identifier.REPEAT_UNTIL.getValue())) {
                if (b.getNestedBlocks() != null && b.getNestedBlocks().size() == 1) {
                    ScBlock nested = b.getNestedBlocks().get(0);
                    if (nested.getContent().startsWith(Identifier.FOREVER.getValue())
                            || nested.getContent().startsWith(Identifier.REPEAT.getValue())
                            || nested.getContent().startsWith(Identifier.REPEAT_UNTIL.getValue())) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    }
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks3(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks3(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }


    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().replace("\"", "").startsWith(Identifier.LEGACY_FOREVER.getValue())
                    || b.getContent().replace("\"", "").startsWith(Identifier.LEGACY_FOREVER_IF.getValue())
                    || b.getContent().replace("\"", "").startsWith(Identifier.LEGACY_REPEAT.getValue())
                    || b.getContent().replace("\"", "").startsWith(Identifier.LEGACY_REPEAT_UNTIL.getValue())) {
                if (b.getNestedBlocks() != null && b.getNestedBlocks().size() == 1) {
                    ScBlock nested = b.getNestedBlocks().get(0);
                    if (nested.getContent().replace("\"", "").startsWith(Identifier.LEGACY_FOREVER.getValue())
                            || nested.getContent().replace("\"", "").startsWith(Identifier.LEGACY_FOREVER_IF.getValue())
                            || nested.getContent().replace("\"", "").startsWith(Identifier.LEGACY_REPEAT.getValue())
                            || nested.getContent().replace("\"", "").startsWith(Identifier.LEGACY_REPEAT_UNTIL.getValue())) {
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