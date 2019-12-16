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

import java.util.Arrays;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.Program;
import scratch.ast.visitor.ScratchVisitor;
import scratch.data.ScBlock;
import scratch.data.Script;
import scratch.structure.Sprite;

/**
 * Checks for missing for-loops in movement scripts.
 */
public class LaggyMovement implements IssueFinder, ScratchVisitor {

    public static final String name = "stuttering_movement";
    private boolean found = false;
    private int count = 0;


    @Override
    public IssueReport check(Program program) {
        throw new RuntimeException("not implemented");
    }

    private int getCount(int count, List<String> pos, Sprite sprite, Script script, List<String> idfs) {
        if (script.getBlocks().size() > 1 && script.getBlocks().get(0).getContent().startsWith(idfs.get(0))) {
            for (ScBlock b : script.getBlocks()) {
                if (b.getContent().startsWith(idfs.get(1))
                    || b.getContent().startsWith(idfs.get(2))
                    || b.getContent().startsWith(idfs.get(3))) {
                    pos.add(sprite.getName() + " at " + Arrays.toString(sprite.getPosition()));
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public String getName() {
        return name;
    }
}
