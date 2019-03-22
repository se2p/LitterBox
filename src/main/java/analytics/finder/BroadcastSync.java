package analytics.finder;

import analytics.Issue;
import analytics.IssueFinder;
import scratch2.data.ScBlock;
import scratch2.data.Script;
import scratch2.structure.Project;
import scratch2.structure.Scriptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Checks if there is a Broadcast and Receive block for every event.
 */
public class BroadcastSync implements IssueFinder {

    @Override
    public Issue check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count = 0;
        List<String> pos = new ArrayList<>();
        List<String> broadcasts = new ArrayList<>();
        List<String> receive = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script != null) {
                    if (script.getBlocks().size() > 1) {
                        searchBlocks(script.getBlocks(), broadcasts);
                        searchBlocksFirst(script.getBlocks(), receive);
                    }
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

        String name = "broadcast_sync";
        return new Issue(name, count, pos, project.getPath(), notes);
    }

    private void searchBlocksFirst(List<ScBlock> blocks, List<String> list) {
        String x = "whenIReceive";
        if (blocks.size() > 0 && blocks.get(0).getContent().replace("\"", "").startsWith(x)) {
            String content = blocks.get(0).getContent().replace("\"", "").replace(x, "");
            list.add(content);
        }
    }

    private void searchBlocks(List<ScBlock> blocks, List<String> list) {
        String x = "broadcast:";
        for (ScBlock b : blocks) {
            if (b.getContent().replace("\"", "").startsWith(x)) {
                String content = b.getContent().replace("\"", "").replace(x, "");
                if (!list.contains(content)) {
                    list.add(content);
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(b.getNestedBlocks(), list);
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(b.getElseBlocks(), list);
            }
        }
    }
}
