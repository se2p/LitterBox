/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package analytics.finder;

import analytics.IssueFinder;
import analytics.IssueReport;
import java.util.ArrayList;
import java.util.List;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Checks if there is a Broadcast and Receive block for every event.
 */
public class BroadcastSync implements IssueFinder {

    String name = "broadcast_sync";

    @Override
    public IssueReport check(Project project) {
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(project.getStage());
        scriptables.addAll(project.getSprites());
        int count;
        List<String> pos = new ArrayList<>();
        List<String> broadcasts = new ArrayList<>();
        List<String> receive = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (project.getVersion().equals(Version.SCRATCH2)) {
                    searchBlocks(script.getBlocks(), broadcasts);
                    searchBlocksFirst(script.getBlocks(), receive);
                } else if (project.getVersion().equals(Version.SCRATCH3)) {
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

        return new IssueReport(name, count, pos, project.getPath(), notes);
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

    private void searchBlocksFirst(List<ScBlock> blocks, List<String> list) {
        String x = Identifier.LEGACY_RECEIVE.getValue();
        if (blocks.get(0).getContent().replace("\"", "").startsWith(x)) {
            String content = blocks.get(0).getContent().replace("\"", "").replace(x, "");
            list.add(content);
        }
    }

    private void searchBlocks(List<ScBlock> blocks, List<String> list) {
        String x = Identifier.LEGACY_BROADCAST.getValue();
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


    @Override
    public String getName() {
        return name;
    }

}
