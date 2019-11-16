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
 * Evaluates the synchronization level of the Scratch program.
 */
public class Synchronization implements IssueFinder {

    private String[] notes = new String[4];
    private String name = "synchronization";

    public Synchronization() {

        notes[0] = "There is a wait block missing.";
        notes[1] = "Basic Level. There is broadcast, receive message, stop "
                + "all or stop program sprite missing.";
        notes[2] = "Developing Level. There is wait until, backdrop change or"
                + " broadcast and wait missing.";
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

    @Override
    public String getName() {
        return name;
    }
}
