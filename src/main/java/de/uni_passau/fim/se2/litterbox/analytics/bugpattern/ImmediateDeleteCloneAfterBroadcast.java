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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.List;

public class ImmediateDeleteCloneAfterBroadcast extends AbstractIssueFinder {
    private String NAME = "immediate_delete_clone_after_broadcast";

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        // check size > 1 because there has to be room for a say/think AND a stop stmt
        if (stmts.size() > 1 && stmts.get(stmts.size() - 1) instanceof DeleteClone) {
            ASTNode questionableNode = stmts.get(stmts.size() - 2);
            if (questionableNode instanceof Broadcast broadcast) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_SPRITE, currentActor.getIdent().getName());
                if (broadcast.getMessage().getMessage() instanceof StringLiteral stringLiteral) {
                    hint.setParameter(Hint.HINT_MESSAGE, stringLiteral.getText());
                } else {
                    hint.setParameter(Hint.HINT_MESSAGE, IssueTranslator.getInstance().getInfo("message"));
                }

                // TODO: This does not clone the message and metadata, should it?
                StatementReplacementVisitor visitor = new StatementReplacementVisitor(broadcast, new BroadcastAndWait(broadcast.getMessage(), broadcast.getMetadata()));
                ScriptEntity refactoredScript = visitor.apply(getCurrentScriptEntity());

                IssueBuilder issueBuilder = prepareIssueBuilder(questionableNode)
                        .withSeverity(IssueSeverity.LOW)
                        .withHint(hint)
                        .withRefactoring(refactoredScript);
                addIssue(issueBuilder);
            }
        }
        super.visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
