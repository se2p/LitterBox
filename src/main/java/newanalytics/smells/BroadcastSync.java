package newanalytics.smells;

import java.util.ArrayList;
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
 * Checks if there is a Broadcast and Receive block for every event.
 */
public class BroadcastSync implements IssueFinder {

    String name = "broadcast_sync";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        List<String> broadcasts = new ArrayList<>();
        List<String> receive = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (program.getVersion().equals(Version.SCRATCH2)) {
                    searchBlocks(script.getBlocks(), broadcasts);
                    searchBlocksFirst(script.getBlocks(), receive);
                } else if (program.getVersion().equals(Version.SCRATCH3)) {
                    searchBlocks3(script.getBlocks(), broadcasts);
                    searchBlocksFirst3(script.getBlocks(), receive);
                }
            }
        }
        for (String s : broadcasts) {
            boolean found = false;
            for (String s2 : receive) {
                if (s2.equals(s)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                pos.add("Broadcast with no Receive: " + s);
            }
        }
        for (String s : receive) {
            boolean found = false;
            for (String s2 : broadcasts) {
                if (s2.equals(s)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                pos.add("Receive with no Broadcast: " + s);
            }
        }
        count = pos.size();
        String notes = "There is a Receive Block for every Broadcast block.";
        if (count > 0) {
            notes = "One or more Broadcast blocks don't have a corresponding Receive block.";
        }

        return new IssueReport(name, count, pos, program.getPath(), notes);
         */
        throw new RuntimeException("not implemented");
    }

    private void searchBlocksFirst3(List<ScBlock> blocks, List<String> receive) {
        if (blocks.get(0).getContent().startsWith(Identifier.RECEIVE.getValue())) {
            String content = blocks.get(0).getFields().get(Identifier.FIELD_RECEIVE.getValue()).get(0);
            receive.add(content);
        }
    }

    private void searchBlocks3(List<ScBlock> blocks, List<String> broadcasts) {
        for (ScBlock b : blocks) {
            if (b.getContent().startsWith(Identifier.BROADCAST.getValue()) || b.getContent().startsWith(Identifier.BROADCAST_WAIT.getValue())) {
                String content = b.getInputs().get(Identifier.FIELD_BROADCAST.getValue()).get(2);
                if (!broadcasts.contains(content)) {
                    broadcasts.add(content);
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks3(b.getNestedBlocks(), broadcasts);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks3(b.getElseBlocks(), broadcasts);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
