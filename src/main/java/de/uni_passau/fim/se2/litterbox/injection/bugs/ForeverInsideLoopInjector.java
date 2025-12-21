package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideLoop;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Injects the ForeverInsideLoop bug by appending a forever loop inside outer loops.
 * Only loops that have successor statements are considered (otherwise the bug has no effect).
 */
public class ForeverInsideLoopInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new ForeverInsideLoop();
    }

    private void inject(LoopStmt originalLoop) {
        // Only inject if the loop has successor statements
        if (!hasSuccessorStatements(originalLoop)) {
            return;
        }

        if (originalLoop.getStmtList().hasStatements()) {
            addMutation(originalLoop, clonedLoop -> {
                List<Stmt> stmts = clonedLoop.getStmtList().getStmts();

                // Wrap the last statement in a forever loop
                int lastIndex = stmts.size() - 1;
                Stmt lastStmt = stmts.remove(lastIndex);

                List<Stmt> foreverBodyStmts = new java.util.ArrayList<>();
                foreverBodyStmts.add(lastStmt);
                StmtList foreverBody = new StmtList(foreverBodyStmts);
                lastStmt.setParentNode(foreverBody);

                RepeatForeverStmt foreverStmt = nodeFactory.createForever(foreverBody);
                foreverBody.setParentNode(foreverStmt);

                stmts.add(foreverStmt);
                foreverStmt.setParentNode(clonedLoop.getStmtList());
            });
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        inject(node);
        super.visit(node);
    }

    @Override
    public void visit(UntilStmt node) {
        inject(node);
        super.visit(node);
    }
}
