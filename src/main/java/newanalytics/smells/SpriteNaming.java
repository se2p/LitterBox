package newanalytics.smells;

import java.util.ArrayList;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.structure.Project;
import scratch.structure.Scriptable;

/**
 * Checks if all Sprites have different names.
 */
public class SpriteNaming implements IssueFinder {

    private String note1 = "All sprites have a different name and dont start with 'Sprite'.";
    private String note2 = "Some sprites have the same name or start with 'Sprite'.";
    private String name = "sprite_naming";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>(project.getSprites());
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            String name = scable.getName();
            if (name.startsWith("Sprite")) {
                pos.add(scable.getName());
                continue;
            }
            for (Scriptable sc2 : scriptables) {
                if (!sc2.equals(scable)) {
                    if (sc2.getName().replaceAll("[0-9]", "").equals(scable.getName().replaceAll("[0-9]", ""))) {
                        pos.add(scable.getName());
                        break;
                    }
                }
            }
        }
        String notes;
        if (pos.size() > 0) {
            notes = note2;
        } else {
            notes = note1;
        }
        return new IssueReport(name, pos.size(), pos, project.getPath(), notes);
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

}
