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
package de.uni_passau.fim.se2.litterbox.analytics.llm;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.DummyLlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

class LLMProgramQueryAnalyzerTest implements JsonTest {

    @Test
    void testAnalyze() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/movesteps.json");
        DummyLlmApi llmApi = new DummyLlmApi("dummy response");

        LLMProgramQueryAnalyzer analyzer = new LLMProgramQueryAnalyzer(
                llmApi,
                LlmPromptProvider.get(),
                new LlmQuery.CustomQuery("custom user question"),
                new QueryTarget.ProgramTarget(),
                false
        );

        final String response = analyzer.analyze(program);
        final String request = llmApi.getRequests().get(0);

        assertThat(request).contains("custom user question");
        assertThat(request).contains("//Sprite: Sprite1");
        assertThat(request).contains("//Script:");
        assertThat(response).isEqualTo("dummy response");
    }

}
