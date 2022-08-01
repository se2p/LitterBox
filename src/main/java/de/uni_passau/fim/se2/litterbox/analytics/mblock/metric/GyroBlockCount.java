package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.GyroPitchAngle;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.GyroRollAngle;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class GyroBlockCount<T extends ASTNode> extends AbstractRobotMetric<T> {
    public static final String NAME = "gyro_block_count";
    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(GyroPitchAngle node) {
        count++;
    }

    @Override
    public void visit(GyroRollAngle node) {
        count++;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
