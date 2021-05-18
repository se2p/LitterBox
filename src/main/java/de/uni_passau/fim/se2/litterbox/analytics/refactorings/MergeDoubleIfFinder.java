package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeDoubleIf;

import java.util.List;

public class MergeDoubleIfFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(StmtList node) {
        final List<Stmt> stmts = node.getStmts();

        for (int i = 0; i < stmts.size() - 1; i++) {
            // Only merge successive ifs with the same condition
            if (stmts.get(i) instanceof IfThenStmt &&
                    stmts.get(i + 1) instanceof IfThenStmt) {
                IfThenStmt ifThenStmt1 = (IfThenStmt) stmts.get(i);
                IfThenStmt ifThenStmt2 = (IfThenStmt) stmts.get(i + 1);
                if (ifThenStmt1.getBoolExpr().equals(ifThenStmt2.getBoolExpr())) {
                    refactorings.add(new MergeDoubleIf(ifThenStmt1, ifThenStmt2));
                }
            }
        }

        visitChildren(node);
    }

    @Override
    public String getName() {
        return MergeDoubleIf.NAME;
    }
}
