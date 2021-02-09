package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnnecessaryIfAfterUntil extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_if_after_until";
    public static final String NAME_ELSE = "unnecessary_if_after_until_else";

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        for (int i = 0; i < stmts.size() - 1; i++) {
            if (stmts.get(i) instanceof UntilStmt && stmts.get(i + 1) instanceof IfElseStmt) {
                UntilStmt until = (UntilStmt) stmts.get(i);
                IfElseStmt ifElse = (IfElseStmt) stmts.get(i + 1);
                if (until.getBoolExpr().equals(ifElse.getBoolExpr())) {
                    Hint hint = new Hint(NAME_ELSE);
                    addIssue(ifElse, ifElse.getMetadata(), IssueSeverity.LOW, hint);
                }
            } else if (stmts.get(i) instanceof UntilStmt && stmts.get(i + 1) instanceof IfThenStmt) {
                UntilStmt until = (UntilStmt) stmts.get(i);
                IfThenStmt ifThen = (IfThenStmt) stmts.get(i + 1);
                if (until.getBoolExpr().equals(ifThen.getBoolExpr())) {
                    Hint hint = new Hint(NAME);
                    addIssue(ifThen, ifThen.getMetadata(), IssueSeverity.LOW, hint);
                }
            }
        }
        visitChildren(node);
    }

    @Override
    public boolean areCouple(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.areCouple(first, other);
        }

        if (other.getFinder() instanceof MissingLoopSensing) {
            if (other.getActor().equals(first.getActor()) && other.getScriptOrProcedureDefinition().equals(first.getScriptOrProcedureDefinition())) {
                ASTNode otherLoc = other.getCodeLocation();
                while (!(otherLoc instanceof IfElseStmt) && !(otherLoc instanceof IfThenStmt)) {
                    otherLoc = otherLoc.getParentNode();
                }
                return otherLoc.equals(first.getCodeLocation());
            }
        }
        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(NAME_ELSE);
        return keys;
    }
}
