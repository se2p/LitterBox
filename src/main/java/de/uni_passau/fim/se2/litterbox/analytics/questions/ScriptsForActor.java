/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

/**
 * Generates a question asking for the number of scripts belonging to a specific actor.
 *
 */
public class ScriptsForActor extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "scripts_for_actor";
    private static final String STAGE_NAME = "Stage";
    private static final String ACTOR_PARAM = "ACTOR";

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        String actor = ScratchBlocksVisitor.of(node.getIdent());
        String actorText = STAGE_NAME.equals(actor) ? STAGE_NAME : actor;
        QuestionBuilder builder = prepareQuestionBuilder()
                .withQuestionParameter(ACTOR_PARAM, actorText)
                .withCorrectAnswers(Integer.toString(node.getScripts().getSize()));
        addQuestion(builder);
    }

    @Override
    public String getName() {
        return FINDER_NAME;
    }

    @Override
    public ProgramDimension getProgramDimension() {
        return ProgramDimension.TEXT;
    }

    @Override
    public ScopeOfFocus getScopeOfFocus() {
        return ScopeOfFocus.MACRO;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.NUMBER;
    }
}
