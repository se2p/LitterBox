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
package analytics.ctscore;

import analytics.IssueFinder;
import analytics.IssueReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Project;
import scratch.structure.Scriptable;
import utils.Identifier;
import utils.Version;

/**
 * Evaluates the level of user interactivity of the Scratch program.
 */
public class UserInteractivity implements IssueFinder {

    private List<List<String>> ids = new ArrayList<>();
    private List<List<String>> legacyIds = new ArrayList<>();
    private String[] notes = new String[4];
    private String name = "user_interactivity";

    public UserInteractivity() {
        ids.add(0, Collections.singletonList(Identifier.GREEN_FLAG.getValue()));
        ids.add(1, Arrays.asList(Identifier.KEYPRESS.getValue(),
                Identifier.THIS_CLICKED.getValue(),
                Identifier.ASK_WAIT.getValue(), Identifier.SENSE.getValue()));
        ids.add(2, Arrays.asList(Identifier.GREATER_THAN.getValue(),
                Identifier.VIDEO.getValue()));

        legacyIds.add(0,
                Collections.singletonList(Identifier.LEGACY_GREEN_FLAG.getValue()));
        legacyIds.add(1, Arrays.asList(Identifier.LEGACY_KEYPRESS.getValue(),
                Identifier.LEGACY_THIS_CLICKED.getValue(),
                Identifier.LEGACY_ASK_WAIT.getValue(),
                Identifier.LEGACY_SENSE.getValue()));
        legacyIds.add(2,
                Arrays.asList(Identifier.LEGACY_GREATER_THAN.getValue(),
                        Identifier.LEGACY_VIDEO.getValue()));

        notes[0] = "There is a green flag missing.";
        notes[1] = "Basic Level. There is key pressed, sprite clicked, ask "
                + "and wait or mouse blocks missing.";
        notes[2] = "Developing Level. There is %s is > %s, video or audio "
                + "missing.";
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

        List<List<String>> versionIds = checkVersion(project);

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
