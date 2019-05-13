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
 * Checks for empty if or else bodies.
 */
public class EmptyBody implements IssueFinder {

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
                            searchBlocks(scable, script, script.getBlocks(), pos, Identifier.LEGACY_IF.getValue(), Identifier.LEGACY_IF_ELSE.getValue());
                        } else if (project.getVersion().equals(Version.SCRATCH3)) {
                            searchBlocks(scable, script, script.getBlocks(), pos, Identifier.IF.getValue(), Identifier.IF_ELSE.getValue());
                        }
                    }
                }
            }
        }
        count = pos.size();
        String notes = "All 'if' blocks have a body.";
        if (count > 0) {
            notes = "Some 'if' blocks have no body.";
        }

        String name = "empty_body";
        return new Issue(name, count, pos, project.getPath(), notes);
    }


    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos, String ifId, String ifElseId) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(ifElseId)) {
                if (b.getNestedBlocks() == null || b.getNestedBlocks().size() == 0) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
                if (b.getElseBlocks() == null || b.getElseBlocks().size() == 0) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
            } else {
                if (b.getContent().startsWith(ifId)) {
                    if (b.getNestedBlocks() == null || b.getNestedBlocks().size() == 0) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    }
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getNestedBlocks(), pos, ifId, ifElseId);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getElseBlocks(), pos, ifId, ifElseId);
            }
        }
    }

}
