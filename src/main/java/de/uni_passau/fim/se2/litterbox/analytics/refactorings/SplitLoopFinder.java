package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitLoop;

import java.util.ArrayList;
import java.util.List;

/*
repeat:
  A
  B

to

repeat:
  A
repeat:
  B
 */
public class SplitLoopFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(Script script) {

        if (script.getEvent() instanceof Never) {
            // Unconnected blocks
            return;
        }

        if (script.getStmtList().getNumberOfStatements() != 1) {
            return;
        }

        if (!(script.getStmtList().getStatement(0) instanceof LoopStmt)) {
            return;
        }

        LoopStmt loop = (LoopStmt) script.getStmtList().getStatement(0);

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(currentActor);
        script.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        StmtList stmts = loop.getStmtList();
        for (int i = 1; i < stmts.getStmts().size(); i++) {
            Stmt splitPoint = stmts.getStmts().get(i);

            List<Stmt> stmts1 = new ArrayList<>(stmts.getStmts().subList(0, i));
            List<Stmt> stmts2 = new ArrayList<>(stmts.getStmts().subList(i, stmts.getNumberOfStatements()));

            // Dependencies can exist in both directions since this is within a loop
            if (!hasControlDependency(cfg, stmts1, stmts2) &&
                    !hasControlDependency(cfg, stmts2, stmts1) &&
                    !hasTimeDependency(cfg, stmts1, stmts2) &&
                    !hasTimeDependency(cfg, stmts2, stmts1) &&
                    !hasDataDependency(cfg, stmts1, stmts2) &
                    !hasDataDependency(cfg, stmts2, stmts1) &
                    !wouldCreateDataDependency(script, stmts2, stmts1)) {
                refactorings.add(new SplitLoop(script, loop, splitPoint));
            }
        }
    }


    @Override
    public String getName() {
        return SplitLoop.NAME;
    }
}
