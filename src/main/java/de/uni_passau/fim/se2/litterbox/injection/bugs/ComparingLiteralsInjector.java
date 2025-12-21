package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ComparingLiterals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects ComparingLiterals bug by replacing a non-literal operand with a matching literal.
 * The bug finder detects comparisons where BOTH sides are literals (StringLiteral or NumberLiteral).
 * This injector finds comparisons where one side is a literal and the other is not,
 * then replaces the non-literal with a matching literal type.
 * <p>
 * Uses immutable transformations to maintain AST immutability.
 */
public class ComparingLiteralsInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new ComparingLiterals();
    }

    @Override
    public void visit(Equals node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            addImmutableMutation(node, n -> new Equals(n.getOperand1(), matchingLiteral, n.getMetadata()));
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            addImmutableMutation(node, n -> new Equals(matchingLiteral, n.getOperand2(), n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(LessThan node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            addImmutableMutation(node, n -> new LessThan(n.getOperand1(), matchingLiteral, n.getMetadata()));
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            addImmutableMutation(node, n -> new LessThan(matchingLiteral, n.getOperand2(), n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            addImmutableMutation(node, n -> new BiggerThan(n.getOperand1(), matchingLiteral, n.getMetadata()));
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            addImmutableMutation(node, n -> new BiggerThan(matchingLiteral, n.getOperand2(), n.getMetadata()));
        }
        super.visit(node);
    }

    private boolean isLiteral(ComparableExpr expr) {
        return expr instanceof NumberLiteral || expr instanceof StringLiteral;
    }

    /**
     * Creates a literal of the same type as the reference literal.
     * Uses the same value to make comparisons always true (for Equals).
     */
    private ComparableExpr createMatchingLiteral(ComparableExpr reference) {
        if (reference instanceof NumberLiteral num) {
            return new NumberLiteral(num.getValue());
        } else if (reference instanceof StringLiteral str) {
            return new StringLiteral(str.getText());
        }
        // Fallback (shouldn't happen if isLiteral check was done)
        return new NumberLiteral(0);
    }
}
