package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitScript;

import java.util.Optional;

public class SplitScriptFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(Script script) {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        script.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);

        StmtList stmts = script.getStmtList();
        for (int i = 1; i < stmts.getStmts().size(); i++) {
            Stmt splitPoint = stmts.getStmts().get(i);
            Optional<CFGNode> cfgNode = cfg.getNode(splitPoint);
            if (cfgNode.isPresent() && !pdg.hasAnyDependencies(cfgNode.get())) {
                refactorings.add(new SplitScript(script, splitPoint));
            }
        }
    }

    @Override
    public String getName() {
        return SplitScript.NAME;
    }
}
