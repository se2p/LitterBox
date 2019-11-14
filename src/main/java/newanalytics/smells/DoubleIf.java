package newanalytics.smells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Checks for multiple ifs in a row with the same condition.
 */
public class DoubleIf implements IssueFinder {

    String name = "double_condition";

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
        String notes = "There are no if clauses with the same condition twice in a row.";
        if (count > 0) {
            notes = "Some scripts have if clauses with the same condition twice in a row.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (b.getContent().startsWith(Identifier.IF.getValue())) {
                    String content2 = b.getCondition();
                    if (content1.equals(content2)) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                        continue;
                    }
                }
            }
            if (b.getContent().startsWith(Identifier.IF.getValue())) {
                content1 = b.getCondition();
                continue;
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
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (content1.equals(b.getContent())) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    continue;
                }
            }
            if (b.getContent().startsWith(Identifier.LEGACY_IF.getValue())) {
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

    @Override
    public String getName() {
        return name;
    }
}
