package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;

/**
 * It detects usage of Boolean Expressions in the project.
 */
public class BoolExpression extends AbstractIssueFinder {
    public static final String NAME = "boolean_expression";

    @Override
    public void visit(And expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)
                && !(expr.getOperand2() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(AsBool expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(BiggerThan expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)
                && !(expr.getOperand2() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(Equals expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)
                && !(expr.getOperand2() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(LessThan expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)
                && !(expr.getOperand2() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(Not expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(Or expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)
                && !(expr.getOperand2() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
