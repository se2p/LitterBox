package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.DetectDistancePort;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class DistanceBlockCount<T extends ASTNode> implements MetricExtractor<T>, MBlockVisitor {
    public static final String NAME = "distance_block_count";
    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(MBlockNode node) {
        node.accept(this);
    }

    @Override
    public void visit(DetectDistancePort node) {
        count++;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
