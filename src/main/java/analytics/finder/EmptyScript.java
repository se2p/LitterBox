package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Scriptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks if all Sprites have a starting point.
 */
public class EmptyScript implements IssueFinder {

    private List<String> headBlocks = new ArrayList<>();
    private String note1;
    private String note2;
    private String name;

    public EmptyScript() {

        name = "empty_script";
        headBlocks.add("whenClicked");
        headBlocks.add("whenCloned");
        headBlocks.add("whenGreenFlag");
        headBlocks.add("whenIReceive");
        headBlocks.add("whenKeyPressed");
        headBlocks.add("whenSceneStarts");
        headBlocks.add("whenSensorGreaterThan");
        note1 = "There are no scripts with empty Body in your Project.";
        note2 = "Some of the Sprites contain scripts with a empty body.";

    }

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    for (String head : headBlocks) {
                        if (script.getBlocks().size() == 1 && script.getBlocks().get(0).getContent().replace("\"", "").startsWith(head)) {
                            pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                        }
                    }
                }
            }
        }
        String notes = note1;
        if (pos.size() > 0) {
            notes = note2;
        }

        return new Issue(name, pos.size(), pos, project.getPath(), notes);
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
