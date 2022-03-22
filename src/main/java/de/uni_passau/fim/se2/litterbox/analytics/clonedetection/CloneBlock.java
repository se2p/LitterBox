/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.clonedetection;

import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.ArrayList;
import java.util.List;

public class CloneBlock {

    private List<Integer> positions1 = new ArrayList<>();

    private List<Integer> positions2 = new ArrayList<>();

    public void add(int x, int y) {
        positions1.add(x);
        positions2.add(y);
    }

    public int size() {
        return positions1.size();
    }

    public boolean isEmpty() {
        return positions1.isEmpty();
    }

    public int getLastX() {
        return positions1.get(positions1.size() - 1);
    }

    public int getLastY() {
        return positions2.get(positions2.size() - 1);
    }

    public int getFirstX() {
        return positions1.get(0);
    }

    public int getFirstY() {
        return positions2.get(0);
    }

    public boolean extendsWithGap(CloneBlock other, int gapSize) {
        int diffX = getFirstX() - other.getLastX() - 1;
        if (diffX < 0 || diffX > gapSize) {
            return false;
        }
        int diffY = getFirstY() - other.getLastY() - 1;
        if (Math.abs(diffY) > gapSize) {
            return false;
        }

        return true;
    }

    public void fillPositionMap(boolean[][] filledPositions) {
        for (int i = 0; i < positions1.size(); i++) {
            int x = positions1.get(i);
            int y = positions2.get(i);
            filledPositions[x][y] = true;
        }
    }

    public void fillClone(CodeClone clone, List<Stmt> statements1, List<Stmt> statements2) {
        for (int i = 0; i < positions1.size(); i++) {
            int x = positions1.get(i);
            int y = positions2.get(i);
            clone.addClonedStatement(statements1.get(x), statements2.get(y));
        }
    }
}
