package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * Calculate the length of the longest script or procedure of the project.
 */
public class LengthLongestScript implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "length_longest_script";
    int count = 0;
    private int localCount = 0;

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
        if (!(node.getEvent() instanceof Never)) {
            localCount++;
        }
        visitChildren(node);
        if (localCount > count) {
            count = localCount;
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        localCount = 0;

        //this is for counting the definition block itself
        localCount++;
        visitChildren(node);
        if (localCount > count) {
            count = localCount;
        }
    }

    @Override
    public void visit(StmtList node) {
        localCount = localCount + node.getStmts().size();
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}