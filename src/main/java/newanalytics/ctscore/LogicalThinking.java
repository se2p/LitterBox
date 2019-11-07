package newanalytics.ctscore;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Evaluates the logical thinking level of the program.
 */
public class LogicalThinking implements IssueFinder {

    private List<List<String>> ids = new ArrayList<>();
    private List<List<String>> legacyIds = new ArrayList<>();
    private String[] notes = new String[4];
    private String name = "logical_thinking";

    public LogicalThinking() {
        ids.add(0, Collections.singletonList(Identifier.IF.getValue()));
        ids.add(1, Collections.singletonList(Identifier.IF_ELSE.getValue()));
        // TODO Program doesn't check logic operations in if conditions.
        ids.add(2, Arrays.asList(Identifier.AND.getValue(),
                Identifier.OR.getValue(), Identifier.NOT.getValue()));

        legacyIds.add(0,
                Collections.singletonList(Identifier.LEGACY_IF.getValue()));
        legacyIds.add(1,
                Collections.singletonList(Identifier.LEGACY_IF_ELSE.getValue()));
        legacyIds.add(2, Arrays.asList(Identifier.LEGACY_AND.getValue(),
                Identifier.LEGACY_OR.getValue(),
                Identifier.LEGACY_NOT.getValue()));

        notes[0] = "There are no if conditions.";
        notes[1] = "Basic Level. There are no if else conditions.";
        notes[2] = "Developing Level. There are no logic operations.";
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

        for (int i = 0; i < versionIds.size(); i++) {
            for (Scriptable scable : scriptables) {
                for (Script script : scable.getScripts()) {
                    search(scable, script, script.getBlocks(), found,
                            versionIds.get(i));
                }
            }
            if (found.size() > 0) {
                level = i + 1;
                pos.addAll(found);
                found.clear();
            }
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
