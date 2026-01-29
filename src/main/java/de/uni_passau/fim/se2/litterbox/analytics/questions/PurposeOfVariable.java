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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Generates free-text questions asking about the purpose of a variable in the program.
 *
 * <p>Example question: "What is the purpose of the variable [score]?"
 */
public class PurposeOfVariable extends AbstractQuestionFinder {

    private boolean inScript;
    private Set<String> variables;

    @Override
    protected void resetState() {
        super.resetState();
        this.inScript = false;
        this.variables = null;
    }

    @Override
    public void visit(Program node) {
        variables = new LinkedHashSet<>();
        super.visit(node);
        currentScript = null;
        currentProcedure = null;

        for (String variable : variables) {
            QuestionBuilder builder = prepareQuestionBuilder()
                    .withQuestionText(QuestionText.fromKey(getName()))
                    .withQuestionParameter("VARIABLE", variable);
            addQuestion(builder);
        }
    }

    @Override
    public void visit(Script node) {
        inScript = true;
        super.visit(node);
        inScript = false;
    }

    @Override
    public void visit(Variable node) {
        if (inScript) {
            variables.add(ScratchBlocksVisitor.of(node));
        }
    }

    public static final String FINDER_NAME = "purpose_of_variable";

    @Override
    public String getName() {
        return FINDER_NAME;
    }

    @Override
    public ProgramDimension getProgramDimension() {
        return ProgramDimension.PURPOSE;
    }

    @Override
    public ScopeOfFocus getScopeOfFocus() {
        return ScopeOfFocus.ATOM;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.FREE_TEXT;
    }
}
