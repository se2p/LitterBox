package analytics.finder;

import analytics.IssueReport;
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
 * Checks if there are unused custom blocks in the project.
 */
public class Noop implements IssueFinder {

    String name = "no_op";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (project.getVersion().equals(Version.SCRATCH2)) {
                    if (script.getBlocks().size() > 0 && script.getBlocks().get(0).getContent().startsWith(Identifier.LEGACY_CUSTOM_BLOCK.getValue())) {
                        List<String> tempPos = new ArrayList<>();
                        String methodName = script.getBlocks().get(0).getContent().replace(Identifier.LEGACY_CUSTOM_BLOCK.getValue(), "");
                        methodName = methodName.split("\"")[0];
                        for (Script script2 : scable.getScripts()) {
                            searchBlocks(script2.getBlocks(), scable, script2, tempPos, methodName);
                        }
                        if (tempPos.size() == 0) {
                            pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                        }
                    }
                } else if (project.getVersion().equals(Version.SCRATCH3)) {
                    if (script.getBlocks().size() > 0 && script.getBlocks().get(0).getContent().startsWith(Identifier.CUSTOM_BLOCK.getValue())) {
                        List<String> tempPos = new ArrayList<>();
                        String methodName = script.getBlocks().get(0).getProcode();
                        for (Script script2 : scable.getScripts()) {
                            searchBlocks3(script2.getBlocks(), scable, script2, tempPos, methodName);
                        }
                        if (tempPos.size() == 0) {
                            pos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                        }
                    }
                }
            }
        }

        count = pos.size();
        String notes = "There are no unused custom blocks in your project.";
        if (count > 0) {
            notes = "There are unused custom blocks in your project.";
        }

        return new IssueReport(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocks3(List<ScBlock> blocks, Scriptable scable, Script script, List<String> tempPos, String methodName) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getContent().startsWith(Identifier.CUSTOM_BLOCK_CALL.getValue())) {
                    tempPos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                }
                if (block.getNestedBlocks() != null && block.getNestedBlocks().size() > 0) {
                    searchBlocks3(block.getNestedBlocks(), scable, script, tempPos, methodName);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks3(block.getElseBlocks(), scable, script, tempPos, methodName);
                }
            }
        }
    }

    private void searchBlocks(List<ScBlock> blocks, Scriptable scable, Script script, List<String> tempPos, String methodName) {
        if (blocks != null) {
            for (ScBlock block : blocks) {
                if (block.getContent().startsWith(Identifier.LEGACY_CUSTOM_BLOCK_CALL.getValue())) {
                    tempPos.add(scable.getName() + " at " + Arrays.toString(script.getPosition()));
                }
                if (block.getNestedBlocks() != null && block.getNestedBlocks().size() > 0) {
                    searchBlocks(block.getNestedBlocks(), scable, script, tempPos, methodName);
                }
                if (block.getElseBlocks() != null && block.getElseBlocks().size() > 0) {
                    searchBlocks(block.getElseBlocks(), scable, script, tempPos, methodName);
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
