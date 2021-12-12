package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.List;

public class UnnecessaryIf extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_if";

    @Override
    public void visit(StmtList node) {

        final List<Stmt> stmts = node.getStmts();
        StmtList lastList = null;
        for (Stmt s : stmts) {
            if (s instanceof IfThenStmt) {

                if (lastList != null) {
                    if (lastList.equals(((IfThenStmt) s).getThenStmts())) {
                        addIssue(s, s.getMetadata(), IssueSeverity.LOW);
                    }
                }
                lastList = ((IfThenStmt) s).getThenStmts();
            } else {
                // even if we already have a list from an ifstmt before, it only counts if a second ifstmt
                // follows directly after the first.
                lastList = null;
            }
        }

        visitChildren(node);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
