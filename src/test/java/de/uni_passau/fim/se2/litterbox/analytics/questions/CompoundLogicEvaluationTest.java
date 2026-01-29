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

public class CompoundLogicEvaluationTest implements JsonTest {

    @Test
    public void testAndLogic() throws IOException, ParsingException {
        // if <[key space pressed?] and [mouse down?]> then
        String script = """
                when green flag clicked
                if <<key [space v] pressed?> and <mouse down?>> then
                    say [Hello!]
                end
                """;

        Program program = parseScratchBlocks(script);

        CompoundLogicEvaluation finder = new CompoundLogicEvaluation();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);
        Assertions.assertTrue(q.getQuestionText(translator).contains("What must happen for the code inside the 'if' block to execute?"));
        String correctAnswer = q.getCorrectAnswers().iterator().next();

        // Expected formatting: <key [space v] pressed?> AND <mouse down?>
        // ScratchBlocksVisitor emits standard format.
        Assertions.assertTrue(correctAnswer.contains("AND"));
        Assertions.assertTrue(correctAnswer.contains("pressed?") && correctAnswer.contains("mouse down?"));
    }

    @Test
    public void testOrLogic() throws IOException, ParsingException {
        // if <[score v] = (50)> or <[lives v] < (3)> then
        String script = """
                when green flag clicked
                if <<[score v] = (50)> or <[lives v] < (3)>> then
                    stop [all v]
                end
                """;

        Program program = parseScratchBlocks(script);

        CompoundLogicEvaluation finder = new CompoundLogicEvaluation();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        String correctAnswer = q.getCorrectAnswers().iterator().next();
        Assertions.assertTrue(correctAnswer.contains("OR"));
        Assertions.assertTrue(correctAnswer.contains("=") && correctAnswer.contains("<"));
    }
}
