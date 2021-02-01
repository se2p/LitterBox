package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetDragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class InsideControlMotionStmtCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "inside_control_motion_stmt_count";

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
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
