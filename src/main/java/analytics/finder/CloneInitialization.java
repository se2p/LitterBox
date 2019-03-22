package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch2.data.ScBlock;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Scriptable;

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
        int count = 0;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        searchBlocks(scable, script, script.getBlocks(), pos);
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

    private void searchBlocks(Scriptable scable, Script sc, List<ScBlock> blocks, List<String> pos) {
        for (ScBlock b : blocks) {
            if (b.getContent().replace("\"", "").startsWith("createCloneOf_myself_")) {
                boolean check = false;
                for (Script s : scable.getScripts()) {
                    if (s.getBlocks().get(0).getContent().replace("\"", "").startsWith("whenCloned")) {
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
