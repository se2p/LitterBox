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

public class MotionStatementExecutionTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @Test
    public void testMotionExecution() throws IOException, ParsingException {
        // change x by (10)
        // set y to (50)
        String script = """
                when green flag clicked
                change x by (10)
                set y to (50)
                """;

        Program program = parseScratchBlocks(script);

        MotionStatementExecution finder = new MotionStatementExecution();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(2, questions.size());

        boolean foundChangeX = false;
        boolean foundSetY = false;

        for (Question q : questions) {
            String text = q.getQuestionText(translator);
            String correct = q.getCorrectAnswers().iterator().next();
            // Check text contains position and correct answer is reasonable
            if (text.contains("x position") && correct.contains("increases")) {
                foundChangeX = true;
                Assertions.assertEquals("It increases by 10", correct);
            } else if (text.contains("y position") && correct.equals("50")) {
                foundSetY = true;
                Assertions.assertEquals("50", correct);
            }
        }

        Assertions.assertTrue(foundChangeX, "Question for Change X not found");
        Assertions.assertTrue(foundSetY, "Question for Set Y not found");
    }
}

