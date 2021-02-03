package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class InsideControlPenStmtCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "inside_control_pen_stmt_count";
    private int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(PenStmt node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
