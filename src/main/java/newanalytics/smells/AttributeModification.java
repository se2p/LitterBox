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
 * Checks if attributes get modified multiple times in a row.
 */
public class AttributeModification implements IssueFinder {

    String name = "multiple_attribute_modification";

    @Override
    public IssueReport check(Project project) {
        return runCheck(project);
    }

    private IssueReport runCheck(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (project.getVersion().equals(Version.SCRATCH2)) {
                    searchVariableModification(scable, script, script.getBlocks(), pos);
                    searchXYModification(scable, script, script.getBlocks(), pos);
                } else if (project.getVersion().equals(Version.SCRATCH3)) {
                    searchVariableModification3(scable, script, script.getBlocks(), pos);
                    searchXYModification3(scable, script, script.getBlocks(), pos);
                }
            }
        }
        count = pos.size();
        String notes = "No variable gets modified multiple times in a row.";
        if (count > 0) {
            notes = "Some scripts modify the same variable multiple times in a row.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    private void searchVariableModification3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (b.getContent().startsWith(Identifier.CHANGE_VAR.getValue())) {
                    String content2 = b.getFields().get(Identifier.FIELD_VARIABLE.getValue()).get(0);
                    if (content1.equals(content2)) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                        break;
                    }
                }
            }
            if (b.getContent().equals(Identifier.CHANGE_VAR.getValue())) {
                content1 = b.getFields().get(Identifier.FIELD_VARIABLE.getValue()).get(0);

                if (content1 == null) {
                    content1 = "";
                }
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchVariableModification3(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchVariableModification3(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }

    private void searchXYModification3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                if (b.getContent().startsWith(Identifier.CHANGE_X.getValue()) && content1.equals("x") ||
                        b.getContent().startsWith(Identifier.CHANGE_Y.getValue()) && content1.equals("y")) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    break;
                }
            }
            if (b.getContent().startsWith(Identifier.CHANGE_X.getValue())) {
                content1 = "x";
                continue;
            } else if (b.getContent().startsWith(Identifier.CHANGE_Y.getValue())) {
                content1 = "y";
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchXYModification3(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchXYModification3(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }


    private void searchVariableModification(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        String content1 = "";
        for (ScBlock b : blocks) {
            if (!content1.equals("")) {
                String[] parts = b.getContent().replace(Identifier.LEGACY_CHANGE_VAR.getValue(), "").split("\"");
                if (parts.length > 0 && content1.equals(parts[0])) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    break;
                }
            }
            if (b.getContent().startsWith(Identifier.LEGACY_CHANGE_VAR.getValue())) {
                String[] parts = b.getContent().replace(Identifier.LEGACY_CHANGE_VAR.getValue(), "").split("\"");
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
                if (b.getContent().startsWith(Identifier.LEGACY_CHANGEY.getValue()) && content1.equals("y") ||
                        b.getContent().startsWith(Identifier.LEGACY_CHANGEX.getValue()) && content1.equals("x")) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    break;
                }
            }
            if (b.getContent().startsWith(Identifier.LEGACY_CHANGEY.getValue())) {
                content1 = "y";
                continue;
            } else if (b.getContent().startsWith(Identifier.LEGACY_CHANGEX.getValue())) {
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

    @Override
    public String getName() {
        return name;
    }
}
