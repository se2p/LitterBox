package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithTermination;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

import java.util.List;

/**
 * Injects the CustomBlockWithTermination bug by appending StopAll to procedure bodies.
 * Only targets procedures that are called in non-tail position.
 */
public class CustomBlockWithTerminationInjector extends AbstractCustomBlockInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new CustomBlockWithTermination();
    }

    @Override
    protected boolean shouldMutate(ProcedureDefinition def) {
        // Skip if last statement is already a termination or forever loop
        List<Stmt> stmts = def.getStmtList().getStmts();
        if (!stmts.isEmpty()) {
            Stmt lastStmt = stmts.getLast();
            return !(lastStmt instanceof TerminationStmt) && !(lastStmt instanceof RepeatForeverStmt);
        }
        return true;
    }

    @Override
    protected void applyMutation(ProcedureDefinition clonedDef) {
        Stmt stopAll = new StopAll(NonDataBlockMetadata.emptyNonBlockMetadata());
        clonedDef.getStmtList().getStmts().add(stopAll);
        stopAll.setParentNode(clonedDef.getStmtList());
    }
}

