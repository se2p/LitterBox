package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.DeleteControlBlock;

import java.util.List;

public class BlockFinder extends AbstractRefactoringFinder {

    private static final String NAME = "block_finder";

    @Override
    public void visit(ScriptList node) {
        final List<Script> scriptList = node.getScriptList();

        for (Script script : scriptList) {
            List<Stmt> stmts = script.getStmtList().getStmts();
            for (Stmt stmt : stmts) {
                if (stmt instanceof ControlStmt) {
                    refactorings.add(new DeleteControlBlock((ControlStmt) stmt));
                }
            }
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
