package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MyBlocksBlockCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "my_blocks_block_count";

    private int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(CallStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Parameter node) {
        count++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
