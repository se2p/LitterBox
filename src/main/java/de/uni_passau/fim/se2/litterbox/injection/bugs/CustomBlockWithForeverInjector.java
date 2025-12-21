package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithForever;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

import java.util.List;

/**
 * Injects the CustomBlockWithForever bug by wrapping a statement in a forever loop.
 * Only targets procedures that are called in non-tail position.
 * <p>
 * Strategy: Wrap the last statement of the procedure body in a forever loop.
 * This creates a realistic bug where the forever loop contains actual code,
 * and the loop is at the end (where a forever loop must be, as it can't have successors).
 */
public class CustomBlockWithForeverInjector extends AbstractCustomBlockInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new CustomBlockWithForever();
    }

    @Override
    protected boolean shouldMutate(ProcedureDefinition def) {
        List<Stmt> stmts = def.getStmtList().getStmts();

        // Need at least one statement to wrap
        if (stmts.isEmpty()) {
            return false;
        }

        // Skip if already contains a forever loop (don't double-inject)
        return stmts.stream().noneMatch(s -> s instanceof RepeatForeverStmt);
    }

    @Override
    protected void applyMutation(ProcedureDefinition clonedDef) {
        List<Stmt> clonedStmts = clonedDef.getStmtList().getStmts();

        // Wrap the last statement in a forever loop
        int lastIndex = clonedStmts.size() - 1;
        Stmt lastStmt = clonedStmts.remove(lastIndex);

        List<Stmt> loopBodyStmts = new java.util.ArrayList<>();
        loopBodyStmts.add(lastStmt);
        StmtList loopBody = new StmtList(loopBodyStmts);
        lastStmt.setParentNode(loopBody);

        RepeatForeverStmt forever = nodeFactory.createForever(loopBody);
        loopBody.setParentNode(forever);

        clonedStmts.add(forever);
        forever.setParentNode(clonedDef.getStmtList());
    }
}

