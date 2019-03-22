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
 * Checks for multiple ifs in a row with the same condition.
 */
public class DoubleIf implements IssueFinder {

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
        String notes = "There are no if clauses with the same condition twice in a row.";
        if (count > 0) {
            notes = "Some scripts have if clauses with the same condition twice in a row.";
        }

        String name = "double_condition";
        return new Issue(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (content1.equals(b.getContent())) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    continue;
                }
            }
            if (b.getContent().replace("\"", "").startsWith("doIf")) {
                content1 = b.getContent();
                continue;
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
