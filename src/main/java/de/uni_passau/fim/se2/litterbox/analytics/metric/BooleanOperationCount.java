package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * Counts the number of OR, AND and NOT blocks.
 */
public class BooleanOperationCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "boolean_operation_count";
    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(And node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Or node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Not node) {
        count++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
