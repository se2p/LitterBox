package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.MBlockEvent;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.MBlockBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.MBlockNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.string.MBlockStringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MBlockCount<T extends ASTNode> extends AbstractRobotMetric<T> {
    public static final String NAME = "m_block_count";
    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(MBlockEvent node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MBlockStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MBlockStringExpr node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MBlockNumExpr node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MBlockBoolExpr node) {
        count++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
