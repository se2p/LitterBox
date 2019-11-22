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
package newanalytics.smells;

import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.Program;
import scratch.data.ScBlock;
import utils.Identifier;

/**
 * Checks for projects with no single action.
 */
public class NoOpProject implements IssueFinder {

    private String[] operations = {Identifier.MOTION.getValue(), Identifier.LOOKS.getValue(), Identifier.SOUND.getValue(),
        Identifier.LEGACY_FORWARD.getValue(), Identifier.LEGACY_TURN.getValue(), Identifier.LEGACY_HEADING.getValue(),
        Identifier.LEGACY_POINT.getValue(), Identifier.LEGACY_FRONT.getValue(), Identifier.LEGACY_GO.getValue(),
        Identifier.LEGACY_GLIDE.getValue(), Identifier.LEGACY_CHANGE.getValue(), Identifier.LEGACY_SAY.getValue(),
        Identifier.LEGACY_THINK.getValue(), Identifier.LEGACY_HIDE.getValue(), Identifier.LEGACY_SHOW.getValue(),
        Identifier.LEGACY_PLAY_WAIT.getValue(), Identifier.LEGACY_DRUM.getValue(), Identifier.LEGACY_PLAY.getValue()};
    String name = "noop_project";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>();
        scriptables.add(program.getStage());
        scriptables.addAll(program.getSprites());
        int count = 0;
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            for (Script script : scable.getScripts()) {
                if (script.getBlocks().size() > 1) {
                    if (searchBlocks(script.getBlocks())) {
                        String notes = "Your project is not empty and contains actions.";
                        return new IssueReport(name, count, pos, program.getPath(), notes);
                    }
                }
            }
        }
        String notes = "Your project is empty or does not contain any actions.";
        count = 1;
        return new IssueReport(name, count, pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }

    private boolean searchBlocks(List<ScBlock> blocks) {
        for (ScBlock b : blocks) {
            for (String str : operations) {
                if (b.getContent().replace("\"", "").startsWith(str.replace("\"", ""))) {
                    return true;
                }
            }
            if (b.getNestedBlocks() != null && b.getNestedBlocks().size() > 0) {
                searchBlocks(b.getNestedBlocks());
            }
            if (b.getElseBlocks() != null && b.getElseBlocks().size() > 0) {
                searchBlocks(b.getElseBlocks());
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }
}
