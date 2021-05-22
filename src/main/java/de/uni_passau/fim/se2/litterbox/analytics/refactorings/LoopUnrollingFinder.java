package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.LoopUnrolling;

public class LoopUnrollingFinder extends AbstractRefactoringFinder {

    // TODO: What is a suitable number, and how to set it?
    public static final int MAX_UNROLLING = 6;

    @Override
    public void visit(RepeatTimesStmt loop) {
        NumExpr expr = loop.getTimes();
        if (expr instanceof NumberLiteral) {
            // The Scratch UI prevents decimal numbers so this cast is safe
            int value = (int)((NumberLiteral) expr).getValue();
            if (value <= MAX_UNROLLING) {
                refactorings.add(new LoopUnrolling(loop, value));
            }
        }

        visitChildren(loop);
    }


    @Override
    public String getName() {
        return LoopUnrolling.NAME;
    }
}
