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
package de.uni_passau.fim.se2.litterbox.analytics.ctscore;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

/**
 * Evaluates the level of parallelism in the Scratch program.
 */
public class Parallelism implements IssueFinder {

    private String[] notes = new String[4];
    private String name = "parallelism";

    public Parallelism() {

        notes[0] = "There are not two scripts with a green flag.";
        notes[1] = "Basic Level. There are missing scripts on key pressed or "
                + "sprite clicked.";
        notes[2] = "Developing Level. There are missing scripts on receive "
                + "message, create clone, %s is > %s or backdrop change.";
        notes[3] = "Proficiency Level. Good work!";
    }

    /**
     * {@inheritDoc}
     *
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
        int[] flag = new int[3];

        List<List<String>> versionIds = checkVersion(program);

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
     * @param flag   Shows if a identifier was found.
     */
    /*
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

     */
    @Override
    public String getName() {
        return name;
    }
}
