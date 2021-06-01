package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitSlice;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SwapStatements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SwapStatementsFinder extends AbstractDependencyRefactoringFinder {

    private boolean inScript = true;

    private Script currentScript;

    private ControlFlowGraph cfg;

    private ProgramDependenceGraph pdg;

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            return;
        }
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        script.accept(visitor);
        cfg = visitor.getControlFlowGraph();
        pdg = new ProgramDependenceGraph(cfg);
        inScript = true;
        currentScript = script;
        visitChildren(script);
        inScript = false;
        currentScript = null;
    }
    @Override
    public void visit(StmtList stmtList) {
        if (!inScript) {
            return;
        }

        for (int i = 0; i < stmtList.getNumberOfStatements() - 1; i++) {
            Stmt stmt1 = stmtList.getStatement(i);
            Stmt stmt2 = stmtList.getStatement(i + 1);
            Optional<CFGNode> stmt1Node = pdg.getNode(stmt1);
            Optional<CFGNode> stmt2Node = pdg.getNode(stmt2);
            if (stmt1Node.isPresent() && stmt2Node.isPresent() &&
                    !pdg.hasDependency(stmt1Node.get(), stmt2Node.get()) &&
                    !wouldCreateDataDependency(currentScript, Arrays.asList(stmt2), Arrays.asList(stmt1))) {
                refactorings.add(new SwapStatements(stmt1, stmt2));
            }
        }
    }

    protected boolean wouldCreateDataDependency(Script script, List<Stmt> subScript1, List<Stmt> subScript2) {
        List<Stmt> mergedStatements = new ArrayList<>(subScript1);
        mergedStatements.addAll(subScript2);
        Script mergedScript = new Script(script.getEvent(), new StmtList(mergedStatements));
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        mergedScript.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        return pdg.hasDependencyEdge(subScript1, subScript2);
    }

    @Override
    public String getName() {
        return SplitSlice.NAME;
    }
}
