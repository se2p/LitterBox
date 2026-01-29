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

public class WaitStatementExecutionTest implements JsonTest {

    @Test
    public void testWaitExecution() throws IOException, ParsingException {
        // wait (1.5) seconds
        String script = """
                when green flag clicked
                wait (1.5) seconds
                """;

        Program program = parseScratchBlocks(script);

        WaitStatementExecution finder = new WaitStatementExecution();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);
        Assertions.assertTrue(q.getQuestionText(translator).contains("How long does the script wait"));

        String correct = q.getCorrectAnswers().iterator().next();
        Assertions.assertEquals("1.5", correct);

        Set<String> choices = new java.util.HashSet<>(q.getChoices());
        Assertions.assertTrue(choices.contains("1.5"));
        Assertions.assertTrue(choices.contains("0.5") || choices.contains("2.5") || choices.contains("0"));
    }
}
