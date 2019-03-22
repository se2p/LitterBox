package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Scriptable;

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
        headBlocks.add("whenClicked");
        headBlocks.add("whenCloned");
        headBlocks.add("whenGreenFlag");
        headBlocks.add("whenIReceive");
        headBlocks.add("whenKeyPressed");
        headBlocks.add("whenSceneStarts");
        headBlocks.add("whenSensorGreaterThan");
        note1 = "Every Sprite and Stage is correctly initialized and has a starting point.";
        note2 = "Some of the Sprites are not correctly initialized and have no starting point!";

    }

    @Override
    public Issue check(Project project) {
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

        return new Issue(name, count, pos, project.getPath(), notes);
    }

    public String getNote1() {
        return note1;
    }

    public void setNote1(String note1) {
        this.note1 = note1;
    }

    public String getNote2() {
        return note2;
    }

    public void setNote2(String note2) {
        this.note2 = note2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getHeadBlocks() {
        return headBlocks;
    }

    public void setHeadBlocks(List<String> headBlocks) {
        this.headBlocks = headBlocks;
    }
}
