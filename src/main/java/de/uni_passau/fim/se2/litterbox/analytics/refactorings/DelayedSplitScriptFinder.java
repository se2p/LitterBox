package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.ControlDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.TimeDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitScript;

import java.util.ArrayList;
import java.util.List;

public class DelayedSplitScriptFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            // Unconnected blocks
            return;
        }

        ControlFlowGraph cfg = getControlFlowGraphForScript(script);
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        // Find a common prefix of length X
        // Duplicate this prefix


        StmtList stmts = script.getStmtList();
        for (int i = 1; i < stmts.getStmts().size(); i++) {
            Stmt splitPoint = stmts.getStmts().get(i);

            List<Stmt> stmts1 = new ArrayList<>(stmts.getStmts().subList(0, i));
            List<Stmt> stmts2 = new ArrayList<>(stmts.getStmts().subList(i, stmts.getNumberOfStatements()));

            if (!cdg.hasDependencyEdge(stmts1, stmts2)
                    && !tdg.hasDependencyEdge(stmts1, stmts2)
                    && !ddg.hasDependencyEdge(stmts1, stmts2)
                    && !wouldCreateDataDependency(script, stmts2, stmts1)) {
                refactorings.add(new SplitScript(script, splitPoint));
            }
        }
    }

    @Override
    public String getName() {
        return SplitScript.NAME;
    }
}
