package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeLoops;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MergeLoopsFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(ScriptList scriptList) {

        for (Script script1 : scriptList.getScriptList()) {
            for (Script script2 : scriptList.getScriptList()) {
                if (script1 == script2) {
                    continue;
                }

                if (!script1.getEvent().equals(script2.getEvent())) {
                    continue;
                }

                StmtList stmtList1 = script1.getStmtList();
                StmtList stmtList2 = script2.getStmtList();

                if (stmtList1.getNumberOfStatements() != 1 || stmtList2.getNumberOfStatements() != 1) {
                    continue;
                }

                if (!(stmtList1.getStatement(0) instanceof LoopStmt) ||
                        !(stmtList2.getStatement(0) instanceof LoopStmt)) {
                    continue;
                }

                LoopStmt loopStmt1 = (LoopStmt) stmtList1.getStatement(0);
                LoopStmt loopStmt2 = (LoopStmt) stmtList2.getStatement(0);

                if (!hasDependencies(script1, script2, loopStmt1.getStmtList(), loopStmt2.getStmtList())) {
                    refactorings.add(new MergeLoops(script1, script2));
                }
            }
        }
    }

    /*
     * Since the dependency analysis does not take concurrency into account
     * this method simply applies the merge and checks for dependencies in
     * the resulting script that span across the two parent scripts.
     */
    private boolean hasDependencies(Script script1, Script script2, StmtList stmtList1, StmtList stmtList2) {

        MergeLoops refactoring = new MergeLoops(script1, script2);
        Script merged = refactoring.getMergedScript();
        StmtList mergedStatements = ((LoopStmt) merged.getStmtList().getStatement(0)).getStmtList();

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        merged.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);

        Set<Stmt> stmtScript1 = new LinkedHashSet<>(mergedStatements.getStmts().subList(0, stmtList1.getNumberOfStatements()));
        mergedStatements.getStmts().subList(0, stmtList1.getNumberOfStatements()).stream().forEach(s -> stmtScript1.addAll(getTransitiveStatements(s)));

        Set<Stmt> stmtScript2 = new LinkedHashSet<>(mergedStatements.getStmts().subList(stmtList1.getNumberOfStatements(), mergedStatements.getNumberOfStatements()));
        mergedStatements.getStmts().subList(stmtList1.getNumberOfStatements(), mergedStatements.getNumberOfStatements()).stream().forEach(s -> stmtScript2.addAll(getTransitiveStatements(s)));

        Set<Stmt> slice = pdg.backwardSlice(stmtScript2);
        stmtScript1.retainAll(slice);
        return !stmtScript1.isEmpty();
    }

    private Set<Stmt> getTransitiveStatements(ASTNode startNode) {
        return getTransitiveNodes(startNode).stream().filter(node -> node instanceof Stmt).map(Stmt.class::cast).collect(Collectors.toSet());
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
        return MergeLoops.NAME;
    }
}
