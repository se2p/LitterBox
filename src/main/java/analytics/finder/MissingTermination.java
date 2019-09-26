package analytics.finder;

import analytics.IssueReport;
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
 * Checks for missing statements in repeat-until blocks.
 */
public class MissingTermination implements IssueFinder {

    String name = "missing_termination";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (project.getVersion().equals(Version.SCRATCH2)) {
                    searchBlocks(scable, script, script.getBlocks(), pos);
                } else if (project.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks3(scable, script, script.getBlocks(), pos);
                }
            }
        }
        count = pos.size();
        String notes = "All 'repeat until' blocks terminating correctly.";
        if (count > 0) {
            notes = "Some 'repeat until' blocks have no termination statement.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(Identifier.REPEAT_UNTIL.getValue()) && b.getCondition() == null) {
                pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
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
            if (b.getContent().startsWith(Identifier.LEGACY_REPEAT_FALSE.getValue())) {
                pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
