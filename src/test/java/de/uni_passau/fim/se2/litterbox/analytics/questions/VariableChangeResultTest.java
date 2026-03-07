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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

public class VariableChangeResultTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @Test
    public void testSetVariable() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [myVar v] to (5)
                """;

        Program program = parseScratchBlocks(script);
        VariableChangeResult finder = new VariableChangeResult();
        Set<Question> questions = finder.check(program);

        boolean found = questions.stream().anyMatch(q ->
                q.getQuestionText(translator).contains("myVar") &&
                        q.getCorrectAnswers().stream().anyMatch(a -> a.resolveText(translator, translator::getInfo).contains("5"))
        );

        Assertions.assertTrue(found, "Expected question with answer (5) not found. Questions: " + questions);
    }

    @Test
    public void testChangeVariablePositive() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                change [myVar v] by (1)
                """;

        Program program = parseScratchBlocks(script);
        VariableChangeResult finder = new VariableChangeResult();
        Set<Question> questions = finder.check(program);

        boolean found = questions.stream().anyMatch(q ->
                q.getQuestionText(translator).contains("myVar") &&
                        q.getCorrectAnswers().stream().anyMatch(a -> a.resolveText(translator, translator::getInfo).contains("increases by 1"))
        );
        Assertions.assertTrue(found, "Expected question with answer 'it_increases_by|VALUE=1' not found. Questions: " + questions);
    }

    @Test
    public void testChangeVariableNegative() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                change [myVar v] by (-1)
                """;

        Program program = parseScratchBlocks(script);
        VariableChangeResult finder = new VariableChangeResult();
        Set<Question> questions = finder.check(program);

        boolean found = questions.stream().anyMatch(q ->
                q.getQuestionText(translator).contains("myVar") &&
                        q.getCorrectAnswers().stream().anyMatch(a -> a.resolveText(translator, translator::getInfo).contains("decreases by 1"))
        );
        Assertions.assertTrue(found, "Expected question with answer 'it_decreases_by|VALUE=1' not found. Questions: " + questions);
    }
}

