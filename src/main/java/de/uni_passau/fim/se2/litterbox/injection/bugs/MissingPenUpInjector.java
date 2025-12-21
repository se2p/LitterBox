package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingPenUp;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

/**
 * Injects MissingPenUp bug by removing PenUp statements.
 */
public class MissingPenUpInjector extends AbstractPenInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingPenUp();
    }

    @Override
    protected Class<? extends Stmt> getStatementTypeToRemove() {
        return PenUpStmt.class;
    }
}


