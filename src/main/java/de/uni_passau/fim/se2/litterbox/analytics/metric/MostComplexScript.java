package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * Calculates the complexity of the most complex script based on the weighted method count.
 */
public class MostComplexScript implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "most_complex_script";
    int count = 0;
    int localCount = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        this.count = 0;

        program.accept(this);
        return count;
    }

    @Override
    public void visit(Script node) {
        localCount = 0;

        localCount++;
        visitChildren(node);
        if (localCount > count) {
            count = localCount;
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        localCount = 0;

        localCount++;
        visitChildren(node);
        if (localCount > count) {
            count = localCount;
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
