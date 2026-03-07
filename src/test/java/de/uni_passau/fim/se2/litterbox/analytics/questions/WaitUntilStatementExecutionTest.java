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
import java.util.stream.Collectors;

public class WaitUntilStatementExecutionTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @Test
    public void testWaitUntilExecution() throws IOException, ParsingException {
        // wait until <touching [mouse-pointer v]?>
        // switch costume to [costume2 v]
        String script = """
                when green flag clicked
                wait until <touching [mouse-pointer v]?>
                switch costume to [costume2 v]
                """;

        Program program = parseScratchBlocks(script);

        WaitUntilStatementExecution finder = new WaitUntilStatementExecution();
        Set<Question> questions = finder.check(program);

        Assertions.assertEquals(1, questions.size());
        Question q = questions.iterator().next();

        // Check question text includes condition and is properly translated
        Set<String> choices = q.getChoices().stream()
                .map(c -> c.resolveText(translator, translator::getInfo))
                .collect(Collectors.toSet());
        Assertions.assertTrue(choices.contains("yes_term"));
        Assertions.assertTrue(choices.contains("no_term"));
    }
}
