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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.List;

public abstract class RepeatedSubsequenceFinder {

    public static final int MIN_LENGTH = PropertyLoader.getSystemIntProperty("smell.repeated_subsequence.min_length");
    public static final int MIN_OCCURRENCE = PropertyLoader.getSystemIntProperty("smell.repeated_subsequence.min_occurrence");

    private final int minLength;
    private final int minOccurrence;

    protected RepeatedSubsequenceFinder() {
        this.minLength = MIN_LENGTH;
        this.minOccurrence = MIN_OCCURRENCE;
    }

    protected RepeatedSubsequenceFinder(int minLength, int minOccurrence) {
        this.minLength = minLength;
        this.minOccurrence = minOccurrence;
    }

    protected abstract void handleRepetition(StmtList stmtList, List<Stmt> subsequence, int occurrences);

    public void findRepetitions(StmtList statementList) {
        List<Stmt> statements = statementList.getStmts();

        for (int i = 0; i < statements.size(); i++) {
            int maxSequenceLength = (statements.size() - i) / minOccurrence;

            for (int sequenceLength = maxSequenceLength; sequenceLength > 0 && sequenceLength < statements.size() - i; sequenceLength--) {
                List<Stmt> currentSequence = statements.subList(i, i + sequenceLength);
                if (getSequenceLength(currentSequence) < minLength) {
                    // Check for minimal length including statements inside control statements
                    break;
                }
                int numSubsequences = findSubsequences(statements.subList(i, statements.size()), currentSequence);
                if (numSubsequences >= minOccurrence) {
                    handleRepetition(statementList, currentSequence, numSubsequences);
                    i += numSubsequences * currentSequence.size();
                }
            }
        }
    }

    private int findSubsequences(List<Stmt> statements, List<Stmt> subSequence) {
        int num = 0;

        for (int i = 0; i < statements.size() - subSequence.size() + 1; i += subSequence.size()) {
            for (int j = 0; j < subSequence.size(); j++) {
                if (!statements.get(i + j).equals(subSequence.get(j))) {
                    return num;
                }
            }
            num++;
        }

        return num;
    }

    private int getSequenceLength(List<Stmt> statements) {
        int length = statements.size();
        for (Stmt stmt : statements) {
            // FIXME: Simplify conditionals using LoopStmt interface
            if (stmt instanceof IfElseStmt) {
                IfElseStmt ifStmt = (IfElseStmt) stmt;
                length += getSequenceLength(ifStmt.getThenStmts().getStmts()) + getSequenceLength(ifStmt.getElseStmts().getStmts());
            } else if (stmt instanceof IfThenStmt) {
                IfThenStmt ifStmt = (IfThenStmt) stmt;
                length += getSequenceLength(ifStmt.getThenStmts().getStmts());
            } else if (stmt instanceof RepeatForeverStmt) {
                RepeatForeverStmt repeatStmt = (RepeatForeverStmt) stmt;
                length += getSequenceLength(repeatStmt.getStmtList().getStmts());
            } else if (stmt instanceof RepeatTimesStmt) {
                RepeatTimesStmt repeatStmt = (RepeatTimesStmt) stmt;
                length += getSequenceLength(repeatStmt.getStmtList().getStmts());
            } else if (stmt instanceof UntilStmt) {
                UntilStmt repeatStmt = (UntilStmt) stmt;
                length += getSequenceLength(repeatStmt.getStmtList().getStmts());
            }
        }

        return length;
    }

}
