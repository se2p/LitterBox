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
 * Checks for missing loops in event based actions.
 */
public class MissingForever implements IssueFinder {

    private String name = "missing_forever_loop";

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().replace("\"", "").startsWith("whenGreenFlag")) {
                        for (ScBlock b : script.getBlocks()) {
                            if (b.getContent().replace("\"", "").startsWith("doIf[touching:") ||
                                    b.getContent().replace("\"", "").startsWith("doIf[touchingColor:") ||
                                    b.getContent().replace("\"", "").startsWith("doIf[keyPressed:")) {
                                pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                            }
                        }
                    }
                }
            }
        }
        String note = "There is no fishy touching or keyPressed checks without a loop.";
        if (pos.size()> 0) {
            note = "The project contains some fishy touching and / or keyPressed checks without a loop.";

        }
        return new Issue(name, pos.size(), pos, project.getPath(), note);
    }

}
