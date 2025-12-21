package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingPenDown;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

/**
 * Injects MissingPenDown bug by removing PenDown statements.
 */
public class MissingPenDownInjector extends AbstractPenInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingPenDown();
    }

    @Override
    protected Class<? extends Stmt> getStatementTypeToRemove() {
        return PenDownStmt.class;
    }
}

