package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

public class UselessWait extends AbstractIssueFinder {
    public static final String NAME = "useless_wait";
    private int loopCount = 0;
    private int ifCount = 0;

    @Override
    public void visit(WaitSeconds node) {
        StmtList stmtList = (StmtList) node.getParentNode();
        if (stmtList.getNumberOfStatements() == 1) {

            if (loopCount > 0 || ifCount > 0) {
                if (!hasOtherBlocks(stmtList.getParentNode(), stmtList)) {
                    addIssue(node, node.getMetadata());
                }
            } else {
                addIssue(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        loopCount++;
        visitChildren(node);
        loopCount--;
    }

    @Override
    public void visit(UntilStmt node) {
        loopCount++;
        visitChildren(node);
        loopCount--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        loopCount++;
        visitChildren(node);
        loopCount--;
    }

    @Override
    public void visit(IfElseStmt node) {
        ifCount++;
        visitChildren(node);
        ifCount--;
    }

    @Override
    public void visit(IfThenStmt node) {
        ifCount++;
        visitChildren(node);
        ifCount--;
    }

    private boolean hasOtherBlocks(ASTNode node, StmtList stmtList) {
        if (node instanceof ScriptEntity) {
            return false;
        } else if (node instanceof IfElseStmt) {
            if (stmtList != ((IfElseStmt) node).getThenStmts()) {
                boolean hasStmtsOfValue = checkStmtListOfIfElse(((IfElseStmt) node).getThenStmts());
                if (hasStmtsOfValue) {
                    return true;
                }
            } else if (stmtList != ((IfElseStmt) node).getElseStmts()) {
                boolean hasStmtsOfValue = checkStmtListOfIfElse(((IfElseStmt) node).getElseStmts());
                if (hasStmtsOfValue) {
                    return true;
                }
            } else {
                throw new IllegalStateException("An If-Else-block only has two stmtLists so one has to fit and you should never reach this point.");
            }
        }
        StmtList parentStmtList = (StmtList) node.getParentNode();
        boolean hasOtherStmts = checkStmtList(parentStmtList);
        if (hasOtherStmts) {
            return true;
        } else {
            return hasOtherBlocks(parentStmtList.getParentNode(), parentStmtList);
        }
    }

    private boolean checkStmtListOfIfElse(StmtList stmtList) {
        if (stmtList.getNumberOfStatements() > 1) {
            return true;
        } else if (stmtList.getNumberOfStatements() == 0) {
            return false;
        } else {
            Stmt stmt = stmtList.getStatement(0);
            return stmt instanceof WaitSeconds || stmt instanceof WaitUntil;
        }
    }

    private boolean checkStmtList(StmtList stmtList) {
        if (stmtList.getNumberOfStatements() > 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}
