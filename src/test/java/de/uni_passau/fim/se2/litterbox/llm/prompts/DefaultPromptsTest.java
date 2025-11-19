/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.llm.prompts;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

import static com.google.common.truth.Truth.assertThat;

class DefaultPromptsTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    private final DefaultPrompts prompts = new DefaultPrompts(translator);

    @Test
    void askQuestionAllBlocks() throws IOException, ParsingException {
        final Program program = getAST("src/test/fixtures/looseScript.json");
        final QueryTarget target = new QueryTarget.ProgramTarget();
        final LlmQuery query = new LlmQuery.PredefinedQuery(CommonQuery.EXPLAIN);

        final String prompt = prompts.askQuestion(program, target, query);

        assertThat(prompt).contains("stop all sounds");
        assertThat(prompt).contains("reset timer");
    }

    @Test
    void askQuestionIgnoringLooseBlocks() throws IOException, ParsingException {
        final Program program = getAST("src/test/fixtures/looseScript.json");
        final QueryTarget target = new QueryTarget.ProgramTarget();
        final LlmQuery query = new LlmQuery.PredefinedQuery(CommonQuery.EXPLAIN);

        final String prompt = prompts.askQuestion(program, target, query, true);

        assertThat(prompt).contains("stop all sounds");
        assertThat(prompt).doesNotContain("reset timer");
    }
}
