package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitScript;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

            Set<Stmt> stmts1 = new LinkedHashSet<>(stmts.getStmts().subList(0, i));
            stmts.getStmts().subList(0, i).stream().forEach(s -> stmts1.addAll(getTransitiveStatements(s)));

            Set<Stmt> stmts2 = new LinkedHashSet<>(stmts.getStmts().subList(i, stmts.getNumberOfStatements()));
            stmts.getStmts().subList(i, stmts.getNumberOfStatements()).stream().forEach(s -> stmts2.addAll(getTransitiveStatements(s)));

            boolean hasDependencies = false;
            for (Stmt stmt1 : stmts1) {
                Optional<CFGNode> cfgNode1 = cfg.getNode(stmt1);
                if (!cfgNode1.isPresent()) {
                    continue;
                }

                for (Stmt stmt2 : stmts2) {
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

    private Set<Stmt> getTransitiveStatements(Stmt stmt) {
        return getTransitiveNodes(stmt).stream().filter(node -> node instanceof Stmt).map(Stmt.class::cast).collect(Collectors.toSet());
    }

    private Set<ASTNode> getTransitiveNodes(ASTNode node) {
        Set<ASTNode> nodes = new LinkedHashSet<>();
        nodes.addAll(node.getChildren());
        for (ASTNode child : node.getChildren()) {
            nodes.addAll(getTransitiveNodes(child));
        }
        return nodes;
    }

    @Override
    public String getName() {
        return SplitScript.NAME;
    }
}
