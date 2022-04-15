/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementDeletionVisitor;

import java.util.List;

/**
 * A StopThisScript block at the end of a script is unnecessary
 */
public class UnnecessaryStopScript extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_stop_script";

    @Override
    public void visit(Script script) {
        if (ignoreLooseBlocks && script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }

        List<Stmt> stmts = script.getStmtList().getStmts();
        if (!stmts.isEmpty()) {
            currentScript = script;
            Stmt last = stmts.get(stmts.size() - 1);
            if (last instanceof StopThisScript) {
                StatementDeletionVisitor visitor = new StatementDeletionVisitor(last);
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                IssueBuilder builder = prepareIssueBuilder(last)
                        .withSeverity(IssueSeverity.HIGH).withHint(new Hint(getName()))
                        .withRefactoring(refactoring);

                addIssue(builder);
            }
        }
    }


    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
