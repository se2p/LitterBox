package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Size;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Backdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class LooksBlockCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "looks_block_count";

    private int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(SpriteLookStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(ActorLookStmt node) {
        //these nodes are an ActorLookStmt but not from Look category in Scratch
        if (node instanceof AskAndWait || node instanceof HideList || node instanceof HideVariable || node instanceof ShowList || node instanceof ShowVariable) {
            return;
        }
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Size node) {
        count++;
    }

    @Override
    public void visit(Backdrop node) {
        count++;
    }

    @Override
    public void visit(Costume node) {
        count++;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
