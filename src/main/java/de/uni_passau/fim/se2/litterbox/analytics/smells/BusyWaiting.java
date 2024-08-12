/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This finder looks for a forever loop that contains an if loop that stops at least the script.
 */
public class BusyWaiting extends AbstractIssueFinder {
    public static final String NAME = "busy_waiting";
    public static final String ALL_HINT = "busy_waiting_all";
    public static final String SCRIPT_HINT = "busy_waiting_script";
    public static final String CLONE_HINT = "busy_waiting_clone";
    private boolean insideForeverAndIf;
    private boolean hasStop;
    private RepeatForeverStmt foreverLoop;
    private Hint hint;

    @Override
    public void visit(RepeatForeverStmt node) {
        if (node.getStmtList().getStmts().size() == 1) {
            foreverLoop = node;
        }
        visitChildren(node);
        foreverLoop = null;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (foreverLoop != null && node.getParentNode().getParentNode() instanceof RepeatForeverStmt) {
            insideForeverAndIf = true;
            hasStop = false;
        }
        visitChildren(node);
        if (foreverLoop != null && hasStop) {

            List<Stmt> statements = new ArrayList<>(node.getThenStmts().getStmts());
            statements.remove(statements.size() - 1); // stop statement
            WaitUntil waitUntil = new WaitUntil(node.getBoolExpr(), node.getMetadata());
            statements.add(0, waitUntil);
            StatementReplacementVisitor visitor = new StatementReplacementVisitor(foreverLoop, statements);
            ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
            IssueBuilder builder = prepareIssueBuilder(node)
                    .withSeverity(IssueSeverity.LOW)
                    .withHint(hint)
                    .withRefactoring(refactoring);
            addIssue(builder);
            hasStop = false;
        }
        insideForeverAndIf = false;
    }

    @Override
    public void visit(StopAll node) {
        if (insideForeverAndIf) {
            hasStop = true;
            hint = new Hint(ALL_HINT);
        }
    }

    @Override
    public void visit(StopThisScript node) {
        if (insideForeverAndIf) {
            hasStop = true;
            hint = new Hint(SCRIPT_HINT);
        }
    }

    @Override
    public void visit(DeleteClone node) {
        if (insideForeverAndIf) {
            hasStop = true;
            hint = new Hint(CLONE_HINT);
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

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(CLONE_HINT);
        keys.add(SCRIPT_HINT);
        keys.add(ALL_HINT);
        return keys;
    }
}
