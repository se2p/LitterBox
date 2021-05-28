package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnLeft;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;

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
    public void visit(BiggerThan expr) {
        if (!(isLiteral(expr.getOperand1()) && isLiteral(expr.getOperand2()))) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(Equals expr) {
        if (!(isLiteral(expr.getOperand1()) && isLiteral(expr.getOperand2()))) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(LessThan expr) {
        if (!(isLiteral(expr.getOperand1()) && isLiteral(expr.getOperand2()))) {
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

    private boolean isLiteral(ASTNode node) {
       return (node instanceof NumberLiteral || node instanceof StringLiteral);
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
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
