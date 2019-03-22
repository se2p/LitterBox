package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch2.data.ScBlock;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Sprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks for missing for-loops in movement scripts.
 */
public class LaggyMovement implements IssueFinder {

    @Override
    public Issue check(Project project) {
        List<Sprite> sprites = project.getSprites();
        int count = 0;
        List<String> pos = new ArrayList<>();
        for (Sprite sprite : sprites) {
            for (Script script : sprite.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().replace("\"", "").startsWith("whenKeyPressed")) {
                        for (ScBlock b : script.getBlocks()) {
                            if (b.getContent().replace("\"", "").startsWith("forward:")
                                    || b.getContent().replace("\"", "").startsWith("changeXposBy:")
                                    || b.getContent().replace("\"", "").startsWith("changeYposBy:")) {
                                pos.add(sprite.getName() + " at " + Arrays.toString(sprite.getPosition()));
                                count++;
                            }
                        }
                    }
                }
            }
        }
        String notes = "All movement scripts work fine or there is no \"whenKeyPressed\" movement in the project.";
        if (count > 0) {
            notes = "Some of your user input movement scripts are laggy. Try using a forever loop!";
        }

        String name = "laggy_movement";
        return new Issue(name, count, pos, project.getPath(), notes);
    }
}
