package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class EventsBlockCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "events_block_count";

    private int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(Event node) {
        //StartedAsClone is not in the Events category in Scratch
        if (node instanceof StartedAsClone || node instanceof Never) {
            return;
        }
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Broadcast node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(BroadcastAndWait node) {
        count++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
