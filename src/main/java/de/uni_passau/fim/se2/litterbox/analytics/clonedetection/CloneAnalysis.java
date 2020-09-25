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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.*;
import java.util.stream.Collectors;

public class CloneAnalysis {

    public static final int MIN_SIZE = 3;

    private int minSize = MIN_SIZE;

    private ActorDefinition actor;
    private ASTNode root1;
    private ASTNode root2;

    public CloneAnalysis(ActorDefinition actor) {
        this.actor = actor;
    }

    public CloneAnalysis(ActorDefinition actor, int minSize) {
        this.actor = actor;
        this.minSize = minSize;
    }

    /**
     * Check a fraction of the AST for internal clones, i.e., compare it against itself
     * @param root starting point in the AST for the clone analysis
     * @return all clones found
     */
    public Set<CodeClone> check(ASTNode root) {
        return check(root, root);
    }

    /**
     * Check a pair of ASTs for clones
     * @param root1 first tree
     * @param root2 second tree
     * @return all clones found
     */
    public Set<CodeClone> check(ASTNode root1, ASTNode root2) {

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
        boolean[][] similarityMatrix = getMatrix(normalizedStatements1, normalizedStatements2);

        // Return all clones identifiable in the matrix
        return check(statements1, statements2, similarityMatrix);
    }


    private Set<CodeClone> check(List<Stmt> statements1, List<Stmt> statements2, boolean[][] similarityMatrix) {

        Set<CodeClone> clones = new LinkedHashSet<>();

        for (int i = 0; i < statements1.size(); i++) {
            for (int j = 0; j < statements2.size(); j++) {
                // LinkedHashMap remembers order of insertion
                LinkedHashMap<Integer, Integer> positions = findDiagonale(similarityMatrix, i, j);
                if (positions.size() >= minSize) {
                    CodeClone clone = new CodeClone(actor, root1, root2);
                    for (Map.Entry<Integer, Integer> location : positions.entrySet()) {
                        Stmt stmt1 = statements1.get(location.getKey());
                        Stmt stmt2 = statements2.get(location.getValue());
                        clone.addClonedStatement(stmt1, stmt2);
                    }
                    if (clones.stream().noneMatch(c -> c.contains(clone))) {
                        clones.add(clone);
                    }
                }
            }
        }

        return clones;
    }

    private LinkedHashMap<Integer, Integer> findDiagonale(boolean[][] similarityMatrix, int x, int y) {
        LinkedHashMap<Integer, Integer> positions = new LinkedHashMap<>();
        // TODO allow gaps
        while (x < similarityMatrix.length && y < similarityMatrix[x].length && similarityMatrix[x][y]) {
            positions.put(x, y);
            x++;
            y++;
        }
        return positions;
    }

    private boolean[][] getMatrix(List<Stmt> normalizedStatements1, List<Stmt> normalizedStatements2) {
        int width  = normalizedStatements1.size();
        int height = normalizedStatements2.size();
        boolean[][] matrix = new boolean[width][height];

        // TODO: This makes redundant comparisons -- we compare each pair of nodes twice
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                matrix[i][j] = normalizedStatements1.get(i).equals(normalizedStatements2.get(j));
            }
        }

        return matrix;
    }

}
