package newanalytics.smells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.newast.model.Program;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Checks for missing loops in event based actions.
 */
public class MissingForever implements IssueFinder {

    String name = "missing_forever_loop";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (program.getVersion().equals(Version.SCRATCH2)) {
                    if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(Identifier.LEGACY_GREEN_FLAG.getValue())) {
                        for (ScBlock b : script.getBlocks()) {
                            checkMovement(pos, scable, script, b);
                        }
                    }
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(Identifier.GREEN_FLAG.getValue())) {
                        for (ScBlock b : script.getBlocks()) {
                            checkMovement3(pos, scable, script, b);
                        }
                    }
                }
            }
        }
        String note = "There is no fishy touching or keyPressed checks without a loop.";
        if (pos.size() > 0) {
            note = "The project contains some fishy touching and / or keyPressed checks without a loop.";

        }
        return new IssueReport(name, pos.size(), pos, program.getPath(), note);

         */
        throw new RuntimeException("not implemented");
    }

    private void checkMovement3(List<String> pos, Scriptable scable, Script script, ScBlock b) {
        if (b.getContent().startsWith(Identifier.IF.getValue())) {
            if (b.getCondition().startsWith(Identifier.SENSE_KEYPRESS.getValue())
                    || b.getCondition().startsWith(Identifier.SENSE_TOUCHING.getValue())) {
                pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
