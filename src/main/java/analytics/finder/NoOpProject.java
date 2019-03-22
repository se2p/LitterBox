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
 * Checks for projects with no single action.
 */
public class NoOpProject implements IssueFinder {

    private String[] operations = {"forward:", "turnLeft", "turnRight", "heading:", "pointTowards:", "turnAwayFromEdge", "comeToFront",
            "gotoSpriteOrMouse:", "gotoX", "glideSecs", "change", "xpos:", "ypos:", "setRotationStyle", "say", "think",
            "show", "hide", "hideAll", "lookLike", "startScene", "goBackByLayers", "doPlaySoundAndWait", "drum",
            "noteOn", "playDrum", "playSound", "set", "putPenDown"};

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        String name = "noop_project";
        int count = 0;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        if (searchBlocks(script.getBlocks())) {
                            String notes = "Your project is not empty and contains actions.";
                            return new Issue(name, count, pos, project.getPath(), notes);
                        }
                    }
                }
            }
        }
        String notes = "Your project is empty or does not contain any actions.";
        count = 1;
        return new Issue(name, count, pos, project.getPath(), notes);
    }


    private boolean searchBlocks(List<ScBlock> blocks) {
        for (ScBlock b : blocks) {
            for (String str : operations) {
                if (b.getContent().replace("\"", "").startsWith(str)) {
                    return true;
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(b.getNestedBlocks());
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(b.getElseBlocks());
            }
        }
        return false;
    }

}
