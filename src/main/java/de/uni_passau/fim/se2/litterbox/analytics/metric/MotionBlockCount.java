package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Direction;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PositionX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PositionY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetDragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MotionBlockCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "motion_block_count";

    private int count = 0;
    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(SpriteMotionStmt node) {
        //SetDragMode is a SpriteMotionStmt but not from Motion category in Scratch
        if (node instanceof SetDragMode) {
            return;
        }
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(PositionX node){
        count++;
    }

    @Override
    public void visit(PositionY node){
        count++;
    }

    @Override
    public void visit(Direction node){
        count++;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
