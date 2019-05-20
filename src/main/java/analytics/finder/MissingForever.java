package analytics.finder;

import analytics.IssueReport;
import analytics.IssueFinder;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Scriptable;
import scratch.structure.Project;
import utils.Identifier;
import utils.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks for missing loops in event based actions.
 */
public class MissingForever implements IssueFinder {

    String name = "missing_forever_loop";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (project.getVersion().equals(Version.SCRATCH2)) {
                        if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(Identifier.LEGACY_GREEN_FLAG.getValue())) {
                            for (ScBlock b : script.getBlocks()) {
                                checkMovement(pos, scable, script, b);
                            }
                        }
                    } else if (project.getVersion().equals(Version.SCRATCH3)) {
                        if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(Identifier.GREEN_FLAG.getValue())) {
                            for (ScBlock b : script.getBlocks()) {
                                checkMovement3(pos, scable, script, b);
                            }
                        }
                    }
                }
            }
        }
        String note = "There is no fishy touching or keyPressed checks without a loop.";
        if (pos.size() > 0) {
            note = "The project contains some fishy touching and / or keyPressed checks without a loop.";

        }
        return new IssueReport(name, pos.size(), pos, project.getPath(), note);
    }

    private void checkMovement3(List<String> pos, Scriptable scable, Script script, ScBlock b) {
        if (b.getContent().startsWith(Identifier.IF.getValue())) {
            if (b.getCondition().startsWith(Identifier.SENSE_KEYPRESS.getValue())
                    || b.getCondition().startsWith(Identifier.SENSE_TOUCHING.getValue())) {
                pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
            }
        }
    }

    private void checkMovement(List<String> pos, Scriptable scable, Script script, ScBlock b) {
        if (b.getContent().startsWith(Identifier.LEGACY_IF_TOUCHING.getValue()) ||
                b.getContent().startsWith(Identifier.LEGACY_IF_COLOR.getValue()) ||
                b.getContent().startsWith(Identifier.LEGACY_IF_KEY.getValue())) {
            pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
