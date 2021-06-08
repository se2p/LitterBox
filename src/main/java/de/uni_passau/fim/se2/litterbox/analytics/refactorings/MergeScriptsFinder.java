package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeScripts;

import java.util.ArrayList;
import java.util.List;

public class MergeScriptsFinder extends AbstractDependencyRefactoringFinder {

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

                if (!hasDependencies(script1, script2)) {
                    refactorings.add(new MergeScripts(script1, script2));
                }
            }
        }
    }

    /*
     * Since the dependency analysis does not take concurrency into account
     * this method simply applies the merge and checks for dependencies in
     * the resulting script that span across the two parent scripts.
     */
    private boolean hasDependencies(Script script1, Script script2) {

        MergeScripts refactoring = new MergeScripts(script1, script2);
        Script merged = refactoring.getMergedScript();

        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(currentActor);
        merged.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        List<Stmt> stmtScript1 = new ArrayList<>(merged.getStmtList().getStmts().subList(0, script1.getStmtList().getNumberOfStatements()));
        List<Stmt> stmtScript2 = new ArrayList<>(merged.getStmtList().getStmts().subList(script1.getStmtList().getNumberOfStatements(), merged.getStmtList().getNumberOfStatements()));

        if (hasControlDependency(cfg, stmtScript1, stmtScript2) ||
                hasDataDependency(cfg, stmtScript1, stmtScript2) ||
                hasTimeDependency(cfg, stmtScript1, stmtScript2) ||
                wouldCreateDataDependency(merged, stmtScript2, stmtScript1)) {
            return true;
        }

        return false;
    }


    @Override
    public String getName() {
        return MergeScripts.NAME;
    }
}
