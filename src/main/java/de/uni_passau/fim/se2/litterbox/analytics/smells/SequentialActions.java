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

package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

import java.util.*;

public class SequentialActions extends AbstractIssueFinder {

    private final static int MIN_LENGTH = 2;
    private final static int MIN_OCCURRENCE = 3;

    @Override
    public void visit(StmtList statementList) {
        List<Stmt> statements = statementList.getStmts();

        for (int i = 0; i < statements.size(); i++) {
            int maxSequenceLength = (statements.size() - i) / MIN_OCCURRENCE;

            for (int sequenceLength = maxSequenceLength; sequenceLength > 0 && sequenceLength < statements.size() - i; sequenceLength--) {
                List<Stmt> currentSequence = statements.subList(i, i + sequenceLength);
                if (getSequenceLength(currentSequence) < MIN_LENGTH) {
                    // Check for minimal length including statements inside control statements
                    break;
                }
                int numSubsequences = findSubsequences(statements.subList(i, statements.size()), currentSequence);
                if (numSubsequences >= MIN_OCCURRENCE) {
                    addIssue(statements.get(i), statements.get(i).getMetadata());
                    i += numSubsequences * currentSequence.size();
                }
            }
        }

        super.visit(statementList);
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
            if (stmt instanceof IfElseStmt) {
                IfElseStmt ifStmt = (IfElseStmt) stmt;
                length += getSequenceLength(ifStmt.getStmtList().getStmts()) + getSequenceLength(ifStmt.getElseStmts().getStmts());
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

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return "sequential_actions";
    }
}
