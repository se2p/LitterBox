package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeDoubleIf;

import java.util.List;

public class DoubleIfFinder extends AbstractRefactoringFinder {

    private static final String NAME = "double_if_finder";

    @Override
    public void visit(StmtList node) {
        final List<Stmt> stmts = node.getStmts();
        IfThenStmt if1 = null;
        for (Stmt s : stmts) {
            if (s instanceof IfThenStmt) {
                if (if1 != null && if1.getBoolExpr().equals(((IfThenStmt) s).getBoolExpr())) {
                    refactorings.add(new MergeDoubleIf(if1, (IfThenStmt) s));
                }
                if1 = (IfThenStmt) s;
            } else {
                // even if we already have a condition from an ifstmt before, it only counts if a second ifstmt
                // follows directly after the first.
                if1 = null;
            }
        }

        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
