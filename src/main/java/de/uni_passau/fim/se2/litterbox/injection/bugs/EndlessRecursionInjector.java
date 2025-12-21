package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.EndlessRecursion;
import de.uni_passau.fim.se2.litterbox.ast.builder.ProcedureBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

/**
 * Injector for the {@link EndlessRecursion} bug.
 * <p>
 * This injector inserts a recursive call at the start of a procedure's body,
 * creating an endless recursion (infinite loop through self-call).
 * <p>
 * Only procedures called in non-tail position are targeted, ensuring the
 * endless recursion has observable effects during execution.
 */
public class EndlessRecursionInjector extends AbstractCustomBlockInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new EndlessRecursion();
    }

    @Override
    protected void applyMutation(ProcedureDefinition clonedDef) {
        CallStmt recursiveCall = ProcedureBuilder.createCallTo(clonedDef);
        clonedDef.getStmtList().getStmts().addFirst(recursiveCall);
        recursiveCall.setParentNode(clonedDef.getStmtList());
    }
}
