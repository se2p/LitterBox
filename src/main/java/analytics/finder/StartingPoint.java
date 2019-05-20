package analytics.finder;

import analytics.IssueReport;
import analytics.IssueFinder;
import scratch.data.Script;
import scratch.structure.Scriptable;
import scratch.structure.Project;
import utils.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if all Sprites have a starting point
 */
public class StartingPoint implements IssueFinder {

    private List<String> headBlocks = new ArrayList<>();
    private String note1;
    private String note2;
    private String name;

    public StartingPoint() {
        name = "sprite_starting_point";
        headBlocks.add(Identifier.LEGACY_THIS_CLICKED.getValue());
        headBlocks.add(Identifier.THIS_CLICKED.getValue());
        headBlocks.add(Identifier.LEGACY_START_CLONE.getValue());
        headBlocks.add(Identifier.START_CLONE.getValue());
        headBlocks.add(Identifier.LEGACY_GREEN_FLAG.getValue());
        headBlocks.add(Identifier.GREEN_FLAG.getValue());
        headBlocks.add(Identifier.LEGACY_RECEIVE.getValue());
        headBlocks.add(Identifier.RECEIVE.getValue());
        headBlocks.add(Identifier.LEGACY_KEYPRESS.getValue());
        headBlocks.add(Identifier.KEYPRESS.getValue());
        headBlocks.add(Identifier.LEGACY_BACKDROP.getValue());
        headBlocks.add(Identifier.BACKDROP.getValue());
        headBlocks.add(Identifier.LEGACY_GREATER_THAN.getValue());
        headBlocks.add(Identifier.GREATER_THAN.getValue());
        note1 = "Every Sprite and Stage is correctly initialized and has a starting point.";
        note2 = "Some of the Sprites are not correctly initialized and have no starting point!";

    }

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        boolean hasGreenFlag;
        int count = 0;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            hasGreenFlag = false;
            if (scable.getScripts().size() == 0) {
                hasGreenFlag = true;
            } else {
                for (Script script : scable.getScripts()) {
                    if (script != null) {
                        for (String head : headBlocks) {
                            if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().replace("\"", "").startsWith(head)) {
                                hasGreenFlag = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!hasGreenFlag) {
                pos.add(scable.getName());
                count++;
            }
        }
        String notes = note1;
        if (count > 0) {
            notes = note2;
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    @Override
    public String getName() {
        return name;
    }
}
