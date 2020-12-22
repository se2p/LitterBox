package de.uni_passau.fim.se2.litterbox.analytics.clonedetection;

import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        int diffY = getFirstY() - other.getLastY() - 1;

        if (diffX >= 0 && diffX <= gapSize &&
                Math.abs(diffY) <= gapSize) {
            return true;
        }

        return false;
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
