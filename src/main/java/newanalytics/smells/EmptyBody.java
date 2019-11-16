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
 * Checks for empty if or else bodies.
 */
public class EmptyBody implements IssueFinder {

    String name = "empty_body";

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
                    searchBlocks(scable, script, script.getBlocks(), pos, Identifier.LEGACY_IF.getValue(), Identifier.LEGACY_IF_ELSE.getValue());
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks(scable, script, script.getBlocks(), pos, Identifier.IF.getValue(), Identifier.IF_ELSE.getValue());
                }
            }
        }
        count = pos.size();
        String notes = "All 'if' blocks have a body.";
        if (count > 0) {
            notes = "Some 'if' blocks have no body.";
        }

        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }


    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos, String ifId, String ifElseId) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(ifElseId)) {
                if (b.getNestedBlocks() == null || b.getNestedBlocks().size() == 0) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
            } else {
                if (b.getContent().startsWith(ifId)) {
                    if (b.getNestedBlocks() == null || b.getNestedBlocks().size() == 0) {
                        pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                    }
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getNestedBlocks(), pos, ifId, ifElseId);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getElseBlocks(), pos, ifId, ifElseId);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
