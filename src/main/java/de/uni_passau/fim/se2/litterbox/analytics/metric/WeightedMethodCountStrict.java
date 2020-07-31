package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

/**
 * This visitor calculates the weighted method count as a metric for Scratch projects.
 * It only includes Scripts that do have an Event and can be triggered.
 */
public class WeightedMethodCountStrict extends WeightedMethodCount {
    public static final String NAME = "weighted_method_count_strict";
    private static boolean inScriptOrProcedure;

    @Override
    public String getName() {
        return NAME;
    }

    public void visit(Script node) {
        if (!(node.getEvent() instanceof Never)) {
            inScriptOrProcedure = true;
            count++;
        }
        visitChildren(node);
        inScriptOrProcedure = false;
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        inScriptOrProcedure = true;
        count++;
        visitChildren(procedure);
        inScriptOrProcedure = false;
    }

    @Override
    public void visit(IfElseStmt node) {
        if (inScriptOrProcedure) {
            count++;
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        if (inScriptOrProcedure) {
            count++;
        }
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        if (inScriptOrProcedure) {
            count++;
        }
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        if (inScriptOrProcedure) {
            count++;
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (inScriptOrProcedure) {
            count++;
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (inScriptOrProcedure) {
            count++;
        }
        visitChildren(node);
    }
}
