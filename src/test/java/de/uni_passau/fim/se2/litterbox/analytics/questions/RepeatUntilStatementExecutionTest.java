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

public class RepeatUntilStatementExecutionTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @Test
    public void testRepeatUntilExecution() throws IOException, ParsingException {
        // repeat until <(score) > (50)>
        //     change [score v] by (1)
        // end
        // say [Done!]
        String script = """
                when green flag clicked
                repeat until <(score) > (50)>
                    change [score v] by (1)
                end
                say [Done!]
                """;

        Program program = parseScratchBlocks(script);

        RepeatUntilStatementExecution finder = new RepeatUntilStatementExecution();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        String text = q.getQuestionText(translator);
        System.out.println("DEBUG: Generated Question: " + text);

        // Check question text includes condition (translated)
        Assertions.assertTrue(text.contains("condition"));

        String correct = q.getCorrectAnswers().iterator().next();
        Set<String> choices = new java.util.HashSet<>(q.getChoices());

        // Should have choices and correct answer
        Assertions.assertFalse(choices.isEmpty());
        Assertions.assertNotNull(correct);
    }
}

