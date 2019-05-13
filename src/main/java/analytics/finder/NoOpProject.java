package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Scriptable;
import scratch.structure.Project;
import utils.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for projects with no single action.
 */
public class NoOpProject implements IssueFinder {

    private String[] operations = {Identifier.MOTION.getValue(), Identifier.LOOKS.getValue(), Identifier.SOUND.getValue(),
            Identifier.LEGACY_FORWARD.getValue(), Identifier.LEGACY_TURN.getValue(), Identifier.LEGACY_HEADING.getValue(),
            Identifier.LEGACY_POINT.getValue(), Identifier.LEGACY_FRONT.getValue(), Identifier.LEGACY_GO.getValue(),
            Identifier.LEGACY_GLIDE.getValue(), Identifier.LEGACY_CHANGE.getValue(), Identifier.LEGACY_SAY.getValue(),
            Identifier.LEGACY_THINK.getValue(), Identifier.LEGACY_HIDE.getValue(), Identifier.LEGACY_SHOW.getValue(),
            Identifier.LEGACY_PLAY_WAIT.getValue(), Identifier.LEGACY_DRUM.getValue(), Identifier.LEGACY_PLAY.getValue()};

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
                if (b.getContent().replace("\"", "").startsWith(str.replace("\"", ""))) {
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
