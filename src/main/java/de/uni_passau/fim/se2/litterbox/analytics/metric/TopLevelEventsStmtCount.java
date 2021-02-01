package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class TopLevelEventsStmtCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "top_level_events_stmt_count";
    private int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(Broadcast node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
