package analytics.ctscore;

import analytics.IssueFinder;
import analytics.IssueReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Evaluates the level of flow control of the Scratch program.
 */
public class FlowControl implements IssueFinder {

    private List<List<String>> ids = new ArrayList<>();
    private List<List<String>> legacyIds = new ArrayList<>();
    private String[] notes = new String[4];
    private String name = "flow_control";

    public FlowControl() {
        ids.add(0, Arrays.asList(Identifier.REPEAT.getValue(),
                Identifier.FOREVER.getValue()));
        ids.add(1,
                Collections.singletonList(Identifier.REPEAT_UNTIL.getValue()));

        legacyIds.add(0, Arrays.asList(Identifier.LEGACY_REPEAT.getValue(),
                Identifier.LEGACY_FOREVER.getValue()));
        legacyIds.add(1,
                Collections.singletonList(Identifier.LEGACY_REPEAT_UNTIL.getValue()));

        notes[0] = "There is a sequence of blocks missing.";
        notes[1] = "Basic Level. There is repeat or forever missing.";
        notes[2] = "Developing Level. There is repeat until missing.";
        notes[3] = "Proficiency Level. Good work!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        List<String> pos = new ArrayList<>();
        List<String> found = new ArrayList<>();
        int level = 0;

        List<List<String>> versionIds = checkVersion(project);

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

        return new IssueReport(name, level, pos, project.getPath(),
                notes[level]);
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

    /**
     * Checks the version of the Scratch project and returns the right
     * identifiers for the block keywords.
     *
     * @param project The project to check.
     * @return        The keyword identifiers for the project's version.
     */
    private List<List<String>> checkVersion(Project project) {
        if (project.getVersion().equals(Version.SCRATCH2)) {
            return legacyIds;
        } else {
            return ids;
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
