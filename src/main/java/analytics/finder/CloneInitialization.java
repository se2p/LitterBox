package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks if there is cloning in the project and if all clones are initialized correctly.
 */
public class CloneInitialization implements IssueFinder {

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        if (project.getVersion().equals(Version.SCRATCH2)) {
                            searchBlocks(scable, script, script.getBlocks(), pos);
                        } else if (project.getVersion().equals(Version.SCRATCH3)) {
                            searchBlocks3(project, scable, script, script.getBlocks(), pos);
                        }
                    }
                }
            }
        }
        count = pos.size();
        String notes = "Cloning is done right in this project or does not exist.";
        if (count > 0) {
            notes = "Some scriptables were cloned but never initialized.";
        }

        String name = "clone_initialization";
        return new Issue(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks3(Project project, Scriptable scr, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(Identifier.CREATE_CLONE.getValue())) {
                boolean check = false;
                Scriptable scable = null;
                if (b.getCreatedClone() == null) {
                    break;
                }
                if ("_myself_".equals(b.getCreatedClone())) {
                    scable = scr;
                } else {
                    for (Scriptable scri : project.getSprites()) {
                        if (b.getCreatedClone() != null && b.getCreatedClone().equals(scri.getName())) {
                            scable = scri;
                            break;
                        }
                    }
                }
                if (scable == null) {
                    return;
                }
                for (Script s : scable.getScripts()) {
                    if (s.getBlocks().get(0).getContent().startsWith(Identifier.START_CLONE.getValue())) {
                        check = true;
                        break;
                    }
                }
                if (!check) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks3(project, scr, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks3(project, scr, sc, b.getElseBlocks(), pos);
            }
        }
    }

    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().replace("\"", "").startsWith(Identifier.LEGACY_START_CLONE.getValue())) {
                boolean check = false;
                for (Script s : scable.getScripts()) {
                    if (s.getBlocks().get(0).getContent().replace("\"", "").startsWith(Identifier.LEGACY_CREATE_CLONE.getValue())) {
                        check = true;
                        break;
                    }
                }
                if (!check) {
                    pos.add(scable.getName() + " at " + Arrays.toString(sc.getPosition()));
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getNestedBlocks(), pos);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(scable, sc, b.getElseBlocks(), pos);
            }
        }
    }
}
