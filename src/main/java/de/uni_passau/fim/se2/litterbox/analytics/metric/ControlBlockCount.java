package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class ControlBlockCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "control_block_count";

    private int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(ControlStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        count++;
    }

    @Override
    public void visit(WaitSeconds node) {
        count++;
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        count++;
    }

    @Override
    public void visit(TerminationStmt node) {
        count++;
    }

    @Override
    public void visit(StartedAsClone node) {
        count++;
    }

    @Override
    public void visit(CreateCloneOf node) {
        count++;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
