/*
 * Copyright (C) 2020 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.*;
import java.util.stream.Collectors;

public class CloneAnalysis {

    public static final int MIN_SIZE = 6;
    public static final int MAX_GAP = 2;

    private int minSize = MIN_SIZE;
    private int maxGap = MAX_GAP;

    private ActorDefinition actor;
    private ASTNode root1;
    private ASTNode root2;

    public CloneAnalysis(ActorDefinition actor) {
        this.actor = actor;
    }

    public CloneAnalysis(ActorDefinition actor, int minSize, int maxGap) {
        this.actor = actor;
        this.minSize = minSize;
        this.maxGap = maxGap;
    }

    /**
     * Check a fraction of the AST for internal clones, i.e., compare it against itself
     * @param root starting point in the AST for the clone analysis
     * @return all clones found
     */
    public Set<CodeClone> check(ASTNode root, CodeClone.CloneType cloneType) {
        return check(root, root, cloneType);
    }

    /**
     * Check a pair of ASTs for clones
     * @param root1 first tree
     * @param root2 second tree
     * @return all clones found
     */
    public Set<CodeClone> check(ASTNode root1, ASTNode root2, CodeClone.CloneType cloneType) {

        this.root1 = root1;
        this.root2 = root2;

        // We compare at the level of statements, not tokens
        StatementListVisitor statementListVisitor = new StatementListVisitor();
        List<Stmt> statements1 = statementListVisitor.getStatements(root1);
        List<Stmt> statements2 = statementListVisitor.getStatements(root2);

        // For comparison, we normalize literals and tokens
        NormalizationVisitor normalizationVisitor = new NormalizationVisitor();
        List<Stmt> normalizedStatements1 = statements1.stream().map(normalizationVisitor::apply).collect(Collectors.toList());
        List<Stmt> normalizedStatements2 = statements2.stream().map(normalizationVisitor::apply).collect(Collectors.toList());

        // Comparison matrix on the normalized statements
        boolean[][] similarityMatrix = getSimilarityMatrix(normalizedStatements1, normalizedStatements2);

        // Return all clones identifiable in the matrix
        return check(statements1, statements2, normalizedStatements1, normalizedStatements2, similarityMatrix, cloneType);
    }

    private Set<CodeClone> check(List<Stmt> statements1, List<Stmt> statements2, List<Stmt> normalizedStatements1, List<Stmt> normalizedStatements2, boolean[][] similarityMatrix, CodeClone.CloneType cloneType) {
        Set<CodeClone> clones = new LinkedHashSet<>();
        List<CodeClone> tempClones;
        // LinkedHashMap remembers order of insertion
        LinkedHashMap<Integer, Integer> positions;

        // The following two for-loops could be refactored to not have duplicated code (how ironic)
        // Find clones on the lower left of the matrix, with middle diagonal
        for (int j = 0; j < statements2.size(); j++) {
            positions = findDiagonal(similarityMatrix, 0, j);
            if (!positions.isEmpty()) {
                tempClones = checkDiagonalForGaps(positions, statements1, statements2, normalizedStatements1, normalizedStatements2);

                for (CodeClone clone : tempClones) {
                    if (clone.getType().equals(cloneType) & clone.size() >= minSize) {
                        clones.add(clone);
                    }
                }
            }
        }

        // Find clones on the upper right of the matrix, without middle diagonal
        for (int i = 1; i < statements2.size(); i++) {
            positions = findDiagonal(similarityMatrix, i, 0);
            if (!positions.isEmpty()) {
                tempClones = checkDiagonalForGaps(positions, statements1, statements2, normalizedStatements1, normalizedStatements2);

                for (CodeClone clone : tempClones) {
                    if (clone.getType().equals(cloneType) & clone.size() >= minSize) {
                        clones.add(clone);
                    }
                }
            }
        }

        return clones;
    }

    // Returns positions of duplicated blocks in a diagonal of the similarity matrix
    private LinkedHashMap<Integer, Integer> findDiagonal(boolean[][] similarityMatrix, int x, int y) {
        LinkedHashMap<Integer, Integer> clonePositions = new LinkedHashMap<>();

        while (x < similarityMatrix.length && y < similarityMatrix[x].length) {
            if (similarityMatrix[x][y]) {
                clonePositions.put(x, y);
            }
            x++;
            y++;
        }

        return clonePositions;
    }

    // Every diagonal gets checked, whether it contains gaps or not.
    // A gap larger than maxGap mean the diagonal has multiple clones. A smaller gap indicates a type 3 clone.
    private List<CodeClone> checkDiagonalForGaps(LinkedHashMap<Integer, Integer> positions, List<Stmt> statements1, List<Stmt> statements2, List<Stmt> normalizedStatements1, List<Stmt> normalizedStatements2) {
        Set<Map.Entry<Integer, Integer>> entries = positions.entrySet();
        Map.Entry<Integer, Integer> lastEntry = entries.iterator().next();
        CodeClone clone = new CodeClone(actor, root1, root2);
        List<CodeClone> clones = new ArrayList<>();
        List<Stmt> tempNormalized1 = new ArrayList<>();
        List<Stmt> tempNormalized2 = new ArrayList<>();

        // Could be refactored to not have the same steps duplicated for every clause (again, how ironic)
        for (Map.Entry<Integer, Integer> location : entries) {
            if (location.getKey() - lastEntry.getKey() <= 1) {
                Stmt stmt1 = statements1.get(location.getKey());
                Stmt stmt2 = statements2.get(location.getValue());
                tempNormalized1.add(normalizedStatements1.get(location.getKey()));
                tempNormalized2.add(normalizedStatements2.get(location.getValue()));
                clone.addClonedStatement(stmt1, stmt2);
            } else {
                // maxGap + 1, as maxGap means the number of blocks not being the same
                // 0 1 2 x x 5 6 has two gap blocks, but 5-2=3 > maxGap
                if (location.getKey() - lastEntry.getKey() > maxGap + 1) {
                    // As the gap is larger than maxGap, the clone is finished
                    clone.setType(decideCloneType(clone, tempNormalized1, tempNormalized2));
                    clones.add(clone);

                    // Current location belongs to a new clone
                    clone = new CodeClone(actor, root1, root2);
                    tempNormalized1.clear();
                    tempNormalized2.clear();
                    Stmt stmt1 = statements1.get(location.getKey());
                    Stmt stmt2 = statements2.get(location.getValue());
                    tempNormalized1.add(normalizedStatements1.get(location.getKey()));
                    tempNormalized2.add(normalizedStatements2.get(location.getValue()));
                    clone.addClonedStatement(stmt1, stmt2);
                } else {
                    // If gap is smaller than maxGap allows, gap is part of a type III clone
                    for (int i = 1; lastEntry.getKey() + i < location.getKey(); i++) {
                        Stmt stmt1 = statements1.get(lastEntry.getKey() + i);
                        Stmt stmt2 = statements2.get(lastEntry.getValue() + i);
                        tempNormalized1.add(normalizedStatements1.get(lastEntry.getKey() + i));
                        tempNormalized2.add(normalizedStatements2.get(lastEntry.getValue() + i));
                        clone.addClonedStatement(stmt1, stmt2);
                    }
                }
            }
            lastEntry = location;
        }
        clone.setType(decideCloneType(clone, tempNormalized1, tempNormalized2));
        clones.add(clone);

        return clones;
    }

    private CodeClone.CloneType decideCloneType(CodeClone clone, List<Stmt> normalizedStatements1, List<Stmt> normalizedStatements2){
        List<Stmt> statements1 = clone.getFirstStatements();
        List<Stmt> statements2 = clone.getSecondStatements();
        CodeClone.CloneType type = clone.getType();

        if (normalizedStatements1.equals(normalizedStatements2)) {
            // Type 1 is default, so only test for Type 2 is necessary
            if (!statements1.equals(statements2)) {
                type = CodeClone.CloneType.TYPE2;
            }
        } else {
            // Type 4 not implemented, so no specific check for Type 3 necessary
            type = CodeClone.CloneType.TYPE3;
        }
        return type;
    }

    private boolean[][] getSimilarityMatrix(List<Stmt> normalizedStatements1, List<Stmt> normalizedStatements2) {
        int width  = normalizedStatements1.size();
        int height = normalizedStatements2.size();
        boolean[][] matrix = new boolean[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                matrix[i][j] = normalizedStatements1.get(i).equals(normalizedStatements2.get(j));
            }
        }

        return matrix;
    }

}
