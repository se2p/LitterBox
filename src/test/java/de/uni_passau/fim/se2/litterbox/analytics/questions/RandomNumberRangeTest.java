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

public class RandomNumberRangeTest implements JsonTest {

    @Test
    public void testRandomIntegerRange() throws IOException, ParsingException {
        // pick random (1) to (10)
        String script = """
                when green flag clicked
                say (pick random (1) to (10))
                """;

        Program program = parseScratchBlocks(script);

        RandomNumberRange finder = new RandomNumberRange();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);
        Assertions.assertTrue(q.getQuestionText(translator).startsWith("Which of these values could this block return?"));
        String correctAnswer = q.getCorrectAnswers().iterator().next();

        // Correct answer should be one of the bounds or inside. Finder uses max.
        Assertions.assertEquals("10", correctAnswer);

        // Distractors should include 11, 0, 0.5 (or decimal)
        Set<String> choices = new java.util.HashSet<>(q.getChoices());

        Assertions.assertTrue(choices.contains("10"));
        Assertions.assertTrue(choices.contains("11"));
        Assertions.assertTrue(choices.contains("0"));
        // 0.5 might be formatted as "0.5"
        Assertions.assertTrue(choices.contains("1.5"));
    }
}
