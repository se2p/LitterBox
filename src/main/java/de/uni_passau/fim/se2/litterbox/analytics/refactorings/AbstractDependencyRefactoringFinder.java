package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ControlDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.TimeDependenceGraph;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDependencyRefactoringFinder extends AbstractRefactoringFinder {

    protected boolean hasControlDependency(ControlFlowGraph cfg, List<Stmt> subScript1, List<Stmt> subScript2) {
        // If subScript2 has a control dependency on subScript1 then false, else true
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        // If there exists an edge for any block in subScript2 to a node in subScript1
        return cdg.hasDependencyEdge(subScript1, subScript2);
    }

    protected boolean hasDataDependency(ControlFlowGraph cfg, List<Stmt> subScript1, List<Stmt> subScript2) {
        // If subScript1 has a control dependency on subScript2 then false
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);
        // If subScript2 has a control dependency on subScript1 then false
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
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        mergedScript.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);
        return ddg.hasDependencyEdge(subScript1, subScript2);
    }

}
