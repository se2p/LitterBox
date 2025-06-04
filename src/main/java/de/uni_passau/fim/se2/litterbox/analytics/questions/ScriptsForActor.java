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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * @QuestionType Number
 * @Highlighted Nothing
 * @Context Single actor
 */
public class ScriptsForActor extends AbstractQuestionFinder {

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
        Hint hint = Hint.fromKey(getName());
        String actor = ScratchBlocksVisitor.of(node.getIdent());
        hint.setParameter(Hint.ACTOR, (actor.equals("Stage") ? IssueTranslator.getInstance().getInfo("stage") : actor));
        hint.setParameter(Hint.ANSWER, Integer.toString(node.getScripts().getSize()));
        addIssue(builder.withHint(hint));
    }

    @Override
    public String getName() {
        return "scripts_for_actor";
    }
}
