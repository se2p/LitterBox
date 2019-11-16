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
 * Checks for nested loops.
 */
public class NestedLoops implements IssueFinder {

    String name = "nested_loops";

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
                    searchBlocks(scable, script, script.getBlocks(), pos);
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks3(scable, script, script.getBlocks(), pos);
                }
            }
        }
        count = pos.size();
        String notes = "There are no nested loops in your scripts.";
        if (count > 0) {
            notes = "Some scripts have nested loops.";
        }

        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }

    private void searchBlocks3(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(Identifier.FOREVER.getValue())
                    || b.getContent().startsWith(Identifier.REPEAT.getValue())
                    || b.getContent().startsWith(Identifier.REPEAT_UNTIL.getValue())) {
                if (b.getNestedBlocks() != null && b.getNestedBlocks().size() == 1) {
                    ScBlock nested = b.getNestedBlocks().get(0);
                    if (nested.getContent().startsWith(Identifier.FOREVER.getValue())
                            || nested.getContent().startsWith(Identifier.REPEAT.getValue())
                            || nested.getContent().startsWith(Identifier.REPEAT_UNTIL.getValue())) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    }
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks3(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks3(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}