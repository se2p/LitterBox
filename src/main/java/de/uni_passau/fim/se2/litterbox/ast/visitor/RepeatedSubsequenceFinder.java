package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

import java.util.List;

public abstract class RepeatedSubsequenceFinder {

    // TODO: What is a proper way to handle such constants?
    public final static int MIN_LENGTH = 2;
    public final static int MIN_OCCURRENCE = 3;

    protected abstract void handleRepetition(StmtList stmtList, List<Stmt> subsequence, int occurrences);

    public void findRepetitions(StmtList statementList) {
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
