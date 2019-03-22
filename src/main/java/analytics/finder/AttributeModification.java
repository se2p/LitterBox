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
 * Checks if attributes get modified multiple times in a row.
 */
public class AttributeModification implements IssueFinder {

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
                        searchVariableModification(scable, script, script.getBlocks(), pos);
                        searchXYModification(scable, script, script.getBlocks(), pos);
                    }
                }
            }
        }
        count = pos.size();
        String notes = "No variable gets modified multiple times in a row.";
        if (count > 0) {
            notes = "Some scripts modify the same variable multiple times in a row.";
        }

        String name = "multiple_attribute_modification";
        return new Issue(name, count, pos, project.getPath(), notes);
    }

    private void searchVariableModification(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                String[] parts = b.getContent().replace("\"changeVar:by:\"\"", "").split("\"");
                if (parts.length > 0 && content1.equals(parts[0])) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    break;
                }
            }
            if (b.getContent().replace("\"", "").startsWith("changeVar:by:")) {
                String[] parts = b.getContent().replace("\"changeVar:by:\"\"", "").split("\"");
                if (parts.length > 0) {
                    content1 = parts[0];
                }
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchVariableModification(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchVariableModification(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }

    private void searchXYModification(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (b.getContent().replace("\"", "").startsWith("changeYposBy:") && content1.equals("y") ||
                        b.getContent().replace("\"", "").startsWith("changeXposBy:") && content1.equals("x")) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    break;
                }
            }
            if (b.getContent().replace("\"", "").startsWith("changeYposBy:")) {
                content1 = "y";
                continue;
            } else if (b.getContent().replace("\"", "").startsWith("changeXposBy:")) {
                content1 = "x";
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchXYModification(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchXYModification(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }
}
