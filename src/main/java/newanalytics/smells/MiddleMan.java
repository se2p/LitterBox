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
 * Checks for middleman broadcasts.
 */
public class MiddleMan implements IssueFinder {

    String name = "middle_man";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (program.getVersion().equals(Version.SCRATCH2)) {
                    if (script.getBlocks().size() > 0 && script.getBlocks().get(0).getContent().startsWith(Identifier.LEGACY_RECEIVE.getValue())) {
                        searchBlocks(script.getBlocks(), scable, script, pos);
                    }
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    if (script.getBlocks().size() > 0 && script.getBlocks().get(0).getContent().startsWith(Identifier.RECEIVE.getValue())) {
                        searchBlocks3(script.getBlocks(), scable, script, pos);
                    }
                }
            }
        }

        count = pos.size();
        String notes = "There is no Receive Script that broadcasts an event.";
        if (count > 0) {
            notes = "There is a Receive Script that broadcasts an event.";
        }
        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }

    private void searchBlocks3(List<ScBlock> blocks, Scriptable scable, Script script, List<String> pos) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getContent().startsWith(Identifier.BROADCAST.getValue())
                        || block.getContent().startsWith(Identifier.BROADCAST_WAIT.getValue())) {
                    pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                }
                if (block.getNestedBlocks() != null && block.getNestedBlocks().size() > 0) {
                    searchBlocks3(block.getNestedBlocks(), scable, script, pos);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks3(block.getElseBlocks(), scable, script, pos);
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
