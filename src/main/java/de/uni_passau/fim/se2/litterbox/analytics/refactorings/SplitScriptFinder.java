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

import java.util.List;
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

            List<Stmt> stmtList1 = stmts.getStmts().subList(0, i);
            List<Stmt> stmtList2 = stmts.getStmts().subList(i, stmts.getNumberOfStatements());

            boolean hasDependencies = false;
            for (Stmt stmt1 : stmtList1) {
                Optional<CFGNode> cfgNode1 = cfg.getNode(stmt1);
                if (!cfgNode1.isPresent()) {
                    continue;
                }

                for (Stmt stmt2 : stmtList2) {
                    Optional<CFGNode> cfgNode2 = cfg.getNode(stmt2);
                    if (!cfgNode2.isPresent()) {
                        continue;
                    }
                    if (pdg.hasDependency(cfgNode1.get(), cfgNode2.get())) {
                        hasDependencies = true;
                        break;
                    }
                }
            }

            if (!hasDependencies) {
                refactorings.add(new SplitScript(script, splitPoint));
            }
        }
    }

    @Override
    public String getName() {
        return SplitScript.NAME;
    }
}
