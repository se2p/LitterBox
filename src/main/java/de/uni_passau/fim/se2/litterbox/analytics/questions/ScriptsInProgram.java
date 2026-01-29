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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;

/**
 * Generates a question asking for the total number of scripts in the program.
 *
 */
public class ScriptsInProgram extends AbstractQuestionFinder {

    private int scriptsInProgram;

    @Override
    public void visit(ActorDefinitionList node) {
        scriptsInProgram = 0;
        super.visit(node);

        QuestionBuilder builder = prepareQuestionBuilder()
                .withCorrectAnswers(Integer.toString(scriptsInProgram));
        addQuestion(builder);
    }

    @Override
    public void visit(ActorDefinition node) {
        scriptsInProgram += node.getScripts().getSize();
        currentActor = node;
    }

    @Override
    public String getName() {
        return "scripts_in_program";
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
