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
 * Evaluates the level of parallelism in the Scratch program.
 */
public class Parallelism implements IssueFinder {

    private List<List<String>> ids = new ArrayList<>();
    private List<List<String>> legacyIds = new ArrayList<>();
    private String[] notes = new String[4];
    private String name = "parallelism";

    public Parallelism() {
        ids.add(0, Collections.singletonList(Identifier.GREEN_FLAG.getValue()));
        ids.add(1, Arrays.asList(Identifier.KEYPRESS.getValue(),
                Identifier.THIS_CLICKED.getValue()));
        ids.add(2, Arrays.asList(Identifier.RECEIVE.getValue(),
                Identifier.CREATE_CLONE.getValue(),
                Identifier.GREATER_THAN.getValue(),
                Identifier.BACKDROP.getValue()));

        legacyIds.add(0,
                Collections.singletonList(Identifier.LEGACY_GREEN_FLAG.getValue()));
        legacyIds.add(1, Arrays.asList(Identifier.LEGACY_KEYPRESS.getValue(),
                Identifier.LEGACY_THIS_CLICKED.getValue()));
        legacyIds.add(Arrays.asList(Identifier.LEGACY_RECEIVE.getValue(),
                Identifier.LEGACY_CREATE_CLONE.getValue(),
                Identifier.LEGACY_GREATER_THAN.getValue(),
                Identifier.LEGACY_BACKDROP.getValue()));

        notes[0] = "There are not two scripts with a green flag.";
        notes[1] = "Basic Level. There are missing scripts on key pressed or "
                + "sprite clicked.";
        notes[2] = "Developing Level. There are missing scripts on receive "
                + "message, create clone, %s is > %s or backdrop change.";
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
        int[] flag = new int[3];

        List<List<String>> versionIds = checkVersion(project);

        for (int i = 0; i < versionIds.size(); i++) {
            for (Scriptable scable : scriptables) {
                for (Script script : scable.getScripts()) {
                    flag[i] = search(scable, script, script.getBlocks(), pos,
                            found, versionIds.get(i), flag[i]);
                }
            }
        }

        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == 1) {
                level = i + 1;
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
     * @param flag   Shows if a identifier was found.
     */
    private int search(Scriptable scable, Script sc,
                        List<ScBlock> blocks, List<String> pos,
                        List<String> found,
                        List<String> ids, int flag) {
        for (ScBlock b : blocks) {
            String content = b.getContent();
            if (ids.contains(content)) {
                if (found.contains(content)) {
                    flag = 1;
                }
                found.add(content);
                if (pos.size() < 10) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
                continue;
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                search(scable, sc, b.getNestedBlocks(), pos, found, ids, flag);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                search(scable, sc, b.getElseBlocks(), pos, found, ids, flag);
            }
        }
        return flag;
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
