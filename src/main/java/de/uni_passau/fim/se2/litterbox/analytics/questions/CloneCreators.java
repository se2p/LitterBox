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

import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.CloneChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.WithCloneExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates a question asking which actor creates a clone of another actor.
 *
 */
public class CloneCreators extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "clone_creators";
    private static final String TARGET_PARAM = "TARGET";
    private static final String MYSELF_KEY = "_myself_";
    private static final String MYSELF_VAL = "myself";

    @Override
    public void visit(CreateCloneOf node) {
        String targetName = resolveCloneTarget(node.getCloneChoice());

        // If we can't resolve the target name statically (e.g. variable), skip it.
        if (targetName == null) {
            return;
        }

        String creatorName = currentActor.getIdent().getName();

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionParameter(TARGET_PARAM, targetName)
                .withCorrectAnswers(Collections.singletonList(creatorName))
                .withChoices(getAllActorNames());

        addQuestion(builder);
    }

    private String resolveCloneTarget(CloneChoice choice) {
        if (choice instanceof Myself) {
            return currentActor.getIdent().getName();
        } else if (choice instanceof WithCloneExpr withExpr) {
            Expression expr = withExpr.getExpression();
            if (expr instanceof StringLiteral str) {
                String text = str.getText();
                // Check if it's "myself" (case insensitive) just in case
                if (MYSELF_KEY.equalsIgnoreCase(text) || MYSELF_VAL.equalsIgnoreCase(text)) {
                    return currentActor.getIdent().getName();
                }
                return text;
            } else if (expr instanceof LocalIdentifier id) {
                return id.getName();
            }
        }
        return null;
    }

    private List<String> getAllActorNames() {
        if (program == null || program.getActorDefinitionList() == null) {
            return Collections.emptyList();
        }
        return program.getActorDefinitionList().getDefinitions().stream()
                .map(a -> a.getIdent().getName())
                .collect(Collectors.toList());
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
        return ScopeOfFocus.RELATION;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
