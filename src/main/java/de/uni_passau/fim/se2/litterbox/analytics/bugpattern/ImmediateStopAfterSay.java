/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImmediateStopAfterSay extends AbstractIssueFinder {
    public static final String NAME = "immediate_stop_after_say_think";
    public static final String HINT_MULTIPLE = "immediate_stop_after_say_think_multiple";
    public boolean hasMultipleActorsWithCode;

    public void visit(Program node) {
        List<ActorDefinition> actors = node.getActorDefinitionList().getDefinitions();
        hasMultipleActorsWithCode = false;
        int i = 0;
        for (ActorDefinition actor : actors) {
            List<Script> scripts = actor.getScripts().getScriptList();
            List<ProcedureDefinition> procedures = actor.getProcedureDefinitionList().getList();
            if (!scripts.isEmpty() || !procedures.isEmpty()) {
                i++;
            }
        }
        if (i > 1) {
            hasMultipleActorsWithCode = true;
        }
        super.visit(node);
    }

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        // check size > 1 because there has to be room for a say/think AND a stop stmt
        if (stmts.size() > 1 && stmts.get(stmts.size() - 1) instanceof StopAll) {
            ASTNode questionableNode = stmts.get(stmts.size() - 2);
            Hint hint;
            if (hasMultipleActorsWithCode) {
                hint = new Hint(HINT_MULTIPLE);
            } else {
                hint = new Hint(getName());
            }
            if (questionableNode instanceof Say) {
                hint.setParameter(Hint.HINT_SAY_THINK, IssueTranslator.getInstance().getInfo("say"));
                addIssue(questionableNode, questionableNode.getMetadata(), IssueSeverity.LOW, hint);
            } else if (questionableNode instanceof Think) {
                hint.setParameter(Hint.HINT_SAY_THINK, IssueTranslator.getInstance().getInfo("think"));
                addIssue(questionableNode, questionableNode.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
        super.visitChildren(node);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(HINT_MULTIPLE);
        return keys;
    }
}
