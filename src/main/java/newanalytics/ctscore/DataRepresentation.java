package newanalytics.ctscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Evaluates the level of data representation of the Scratch program.
 */
public class DataRepresentation implements IssueFinder {

    private List<List<String>> ids = new ArrayList<>();
    private List<List<String>> legacyIds = new ArrayList<>();
    private String[] notes = new String[4];
    private String name = "data_representation";

    public DataRepresentation() {
        ids.add(0, Arrays.asList(Identifier.MOTION.getValue(),
                Identifier.LOOKS.getValue()));
        ids.add(1, Collections.singletonList(Identifier.DATA.getValue()));

        legacyIds.add(0, Arrays.asList(Identifier.LEGACY_MOTION.getValue(),
                Identifier.LEGACY_LOOKS.getValue()));
        legacyIds.add(1,
                Collections.singletonList(Identifier.LEGACY_DATA.getValue()));

        notes[0] = "There are no modifiers of sprites properties.";
        notes[1] = "Basic Level. There are operations on variables missing.";
        notes[2] = "Developing Level. There are operations on lists missing.";
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
        int count = 0;

        List<List<String>> versionIds = checkVersion(project);

        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                level = search(scable, script, script.getBlocks(), found,
                        versionIds,0);
            }
        }
        if (found.size() != 0) {
            pos.addAll(found);
        }

        count += checkLists(scriptables);
        if (count == 1) {
            level = 3;
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
     * @param level  The current level of data representation.
     */
    private int search(Scriptable scable, Script sc,
                        List<ScBlock> blocks, List<String> found,
                        List<List<String>> ids, int level) {

        for (ScBlock b : blocks) {
            String content = b.getContent();
            if (content.startsWith(ids.get(1).get(0))) {
                level = 2;
                found.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                return level;
            } else if (content.startsWith(ids.get(0).get(0))
                   || content.startsWith(ids.get(0).get(1)) ){
                level = 1;
                found.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                search(scable, sc, b.getNestedBlocks(), found, ids, level);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                search(scable, sc, b.getElseBlocks(), found, ids, level);
            }
        }
        return level;
    }

    /**
     * Checks if the project uses lists.
     *
     * @param scriptables Scriptable objects.
     * @return            {@code 1} if lists were found, {@code 0} otherwise.
     */
    private int checkLists(List<Scriptable> scriptables) {
        int foundList = 0;
        for (Scriptable scable : scriptables) {
            if (foundList == 1) {
                break;
            }
            if (scable.getLists().size() > 0) {
                ++foundList;
            }
        }
        return foundList;
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
