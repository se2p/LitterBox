package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ControlDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.TimeDependenceGraph;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDependencyRefactoringFinder extends AbstractRefactoringFinder {

    protected boolean endsWithTerminationStatement(StmtList stmtList) {
        int numStatements1 = stmtList.getNumberOfStatements();
        if (numStatements1 > 0) {
            if (isTerminationStatement(stmtList.getStatement(numStatements1 - 1))) {
                return true;
            }
        }
        return false;
    }

    protected boolean isTerminationStatement(ASTNode node) {
        if (node instanceof RepeatForeverStmt ||
                node instanceof TerminationStmt) {
            return true;
        }
        return false;
    }

    protected boolean hasControlDependency(ControlFlowGraph cfg, List<Stmt> subScript1, List<Stmt> subScript2) {
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        // If subScript2 has a control dependency on subScript1 then false, else true
        return cdg.hasDependencyEdge(subScript1, subScript2);
    }

    protected boolean hasDataDependency(ControlFlowGraph cfg, List<Stmt> subScript1, List<Stmt> subScript2) {
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);
        // If subScript2 has a data dependency on subScript1 then false
        return ddg.hasDependencyEdge(subScript1, subScript2);
    }

    protected boolean hasTimeDependency(ControlFlowGraph cfg, List<Stmt> subScript1, List<Stmt> subScript2) {
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);
        // If subScript2 has a time dependency on subScript1 then false, else true
        return tdg.hasDependencyEdge(subScript1, subScript2);
    }

    protected boolean wouldCreateDataDependency(Script script, List<Stmt> subScript1, List<Stmt> subScript2) {
        List<Stmt> mergedStatements = new ArrayList<>(subScript1);
        mergedStatements.addAll(subScript2);
        Script mergedScript = new Script(script.getEvent(), new StmtList(mergedStatements));
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(currentActor);
        mergedScript.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);
        return ddg.hasDependencyEdge(subScript1, subScript2);
    }
}
