package analytics.finder;

import analytics.IssueFinder;
import analytics.IssueReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Checks for sequential actions that can be replaced by (do x times).
 */
public class SequentialActions implements IssueFinder {

    String name = "sequential_actions";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script.getBlocks().size() > 1) {
                    searchVariableModification(scable, script, script.getBlocks(), pos, project);
                }
            }
        }
        count = pos.size();
        String notes = "There are no sequential actions with the same content in your project.";
        if (count > 0) {
            notes = "Some scripts have sequential actions with the same content.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }


    private void searchVariableModification(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos, Project project) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (b.getContent().equals(content1)) {
                pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                    searchVariableModification(scable, sc, b.getNestedBlocks(), pos, project);
                }
                if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                    searchVariableModification(scable, sc, b.getElseBlocks(), pos, project);
                }
                break;
            }
            String toSearch = "";
            if (project.getVersion().equals(Version.SCRATCH2)) {
                toSearch = Identifier.LEGACY_WAIT.getValue();
            } else if (project.getVersion().equals(Version.SCRATCH3)) {
                toSearch = Identifier.WAIT.getValue();
            }
            if (b.getContent().startsWith(toSearch)) {
                continue;
            } else {
                content1 = b.getContent();
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchVariableModification(scable, sc, b.getNestedBlocks(), pos, project);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchVariableModification(scable, sc, b.getElseBlocks(), pos, project);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
