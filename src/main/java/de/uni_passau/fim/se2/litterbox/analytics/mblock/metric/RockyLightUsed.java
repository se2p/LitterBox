package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLight;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLightOff;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class RockyLightUsed<T extends ASTNode> extends AbstractRobotMetric<T> {
    public static final String NAME = "rocky_light_used";
    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        if (count > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void visit(RockyLight node) {
        count++;
    }

    @Override
    public void visit(RockyLightOff node) {
        count++;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
