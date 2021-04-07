package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;

public class BoolExpression extends AbstractIssueFinder {
    public static final String NAME = "boolean_expression";

    @Override
    public void visit(And expr) {
        addIssue(expr, expr.getMetadata(), IssueSeverity.MEDIUM);
        visitChildren(expr);
    }

    @Override
    public void visit(AsBool expr) {
        addIssue(expr, expr.getMetadata(), IssueSeverity.MEDIUM);
        visitChildren(expr);
    }

    @Override
    public void visit(BiggerThan expr) {
        addIssue(expr, expr.getMetadata(), IssueSeverity.MEDIUM);
        visitChildren(expr);
    }

    @Override
    public void visit(Equals expr) {
        addIssue(expr, expr.getMetadata(), IssueSeverity.MEDIUM);
        visitChildren(expr);
    }

    @Override
    public void visit(LessThan expr) {
        addIssue(expr, expr.getMetadata(), IssueSeverity.MEDIUM);
        visitChildren(expr);
    }

    @Override
    public void visit(Not expr) {
        addIssue(expr, expr.getMetadata(), IssueSeverity.MEDIUM);
        visitChildren(expr);
    }

    @Override
    public void visit(Or expr) {
        addIssue(expr, expr.getMetadata(), IssueSeverity.MEDIUM);
        visitChildren(expr);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.GOOD_PRACTICE;
    }
}
