package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class AbstractRobotMetric<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor, MBlockVisitor {
    @Override
    public void visit(MBlockNode node) {
        node.accept((MBlockVisitor) this);
    }

    @Override
    public void visitParentVisitor(MBlockNode node) {
        visitDefaultVisitor(node);
    }
}
