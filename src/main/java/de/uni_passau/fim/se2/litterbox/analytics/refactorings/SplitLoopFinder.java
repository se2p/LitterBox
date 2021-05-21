package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitLoop;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SplitLoopFinder extends AbstractRefactoringFinder {

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

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        script.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);

        StmtList stmts = loop.getStmtList();
        for (int i = 1; i < stmts.getStmts().size(); i++) {
            Stmt splitPoint = stmts.getStmts().get(i);

            // TODO: Calls for extract method to do this nicely
            Set<Stmt> stmts1 = new LinkedHashSet<>(stmts.getStmts().subList(0, i));
            stmts.getStmts().subList(0, i).stream().forEach(s -> stmts1.addAll(getTransitiveStatements(s)));

            Set<Stmt> stmts2 = new LinkedHashSet<>(stmts.getStmts().subList(i, stmts.getNumberOfStatements()));
            stmts.getStmts().subList(i, stmts.getNumberOfStatements()).stream().forEach(s -> stmts2.addAll(getTransitiveStatements(s)));

            Set<Stmt> slice = pdg.backwardSlice(stmts2);
            stmts1.retainAll(slice);
            if (stmts1.isEmpty()) {
                refactorings.add(new SplitLoop(script, loop, splitPoint));
            }
        }
    }

    // TODO: Code clone
    private Set<Stmt> getTransitiveStatements(Stmt stmt) {
        return getTransitiveNodes(stmt).stream().filter(node -> node instanceof Stmt).map(Stmt.class::cast).collect(Collectors.toSet());
    }

    // TODO: Code clone
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
        return SplitLoop.NAME;
    }
}
