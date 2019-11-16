package newanalytics.ctscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.newast.model.Program;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Evaluates the level of flow control of the Scratch program.
 */
public class FlowControl implements IssueFinder {

    private String[] notes = new String[4];
    private String name = "flow_control";

    public FlowControl() {

        notes[0] = "There is a sequence of blocks missing.";
        notes[1] = "Basic Level. There is repeat or forever missing.";
        notes[2] = "Developing Level. There is repeat until missing.";
        notes[3] = "Proficiency Level. Good work!";
    }

    /**
     * {@inheritDoc}
     * @param program
     */
    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        List<String> pos = new ArrayList<>();
        List<String> found = new ArrayList<>();
        int level = 0;

        List<List<String>> versionIds = checkVersion(program);

        for (List<String> id : versionIds) {
            for (Scriptable scable : scriptables) {
                for (Script script : scable.getScripts()) {
                    search(scable, script, script.getBlocks(), found, id);
                }
            }
            if (found.size() > 0) {
                ++level;
                pos.addAll(found);
                found.clear();
            }
        }

        int foundBlockStack = checkBlockStacks(scriptables);
        if (foundBlockStack == 1 && level != 0) {
            ++level;
        } else {
            level += foundBlockStack;
        }

        return new IssueReport(name, level, pos, program.getPath(),
                notes[level]);
         */
        throw new RuntimeException("not implemented");
    }

    /**
     * Searches the scripts of the project for the given identifiers.
     *
     * @param scable Scriptable objects.
     * @param sc     Scripts in the project.
     * @param blocks All blocks that are given in the scripts.
     * @param found  The identifiers that were found.
     * @param ids    The identifiers for the current version of the project.
     */
    private void search(Scriptable scable, Script sc,
                        List<ScBlock> blocks, List<String> found,
                        List<String> ids) {
        for (ScBlock b : blocks) {
            if (ids.contains(b.getContent())) {
                if (found.size() < 10) {
                    found.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                search(scable, sc, b.getNestedBlocks(), found, ids);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                search(scable, sc, b.getElseBlocks(), found, ids);
            }
        }
    }

    /**
     * Checks if there is a set of blocks that is executed one after another.
     *
     * @param scriptables Scriptable objects in the project.
     * @return            {@code 1} if block stacks were found, {@code 0}
     *                    otherwise.
     */
    private int checkBlockStacks(List<Scriptable> scriptables) {
        int found = 0;
        for (Scriptable scable : scriptables) {
            if (scable.getBlockStack().size() > 1) {
                ++found;
                break;
            }
        }
        return found;
    }

    @Override
    public String getName() {
        return name;
    }
}
