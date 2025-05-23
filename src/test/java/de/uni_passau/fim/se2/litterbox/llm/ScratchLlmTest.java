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
package de.uni_passau.fim.se2.litterbox.llm;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.llm.prompts.DefaultPrompts;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScratchLlmTest {

    @Test
    void basicRequestUnparsedResponse() {
        final String response = "keep this response unchanged";
        final ScratchLlm llm = buildLlm(List.of(response));
        assertThat(llm.singleQueryWithTextResponse("ignored")).isEqualTo(response);
    }

    @Test
    void parseableScratchBlocksResponse() {
        final ScratchLlm llm = buildLlm(List.of(buildScript("actor1", "script1")));
        final ParsedLlmResponseCode response = llm.singleQueryWithCodeOnlyResponse("");

        assertThat(response.scripts()).containsKey("actor1");
        assertThat(response.scripts().get("actor1")).containsKey("script1");

        final Script script = (Script) response.script("actor1", "script1");
        assertThat(script).isNotNull();
        assertThat(script.getEvent()).isInstanceOf(GreenFlag.class);
    }

    @Test
    void parseFailedScriptsToScratchBlocks() {
        final LlmResponseParser responseParser = mock(LlmResponseParser.class);
        final ParsedLlmResponseCode code1 = new ParsedLlmResponseCode(
                Collections.emptyMap(),
                Map.of("actor1", Map.of("script1", "invalid", "script2", "invalid2"))
        );
        when(responseParser.parseLLMResponse("response1")).thenReturn(code1);
        final ParsedLlmResponseCode code2 = new ParsedLlmResponseCode(
                Map.of("actor1", Map.of("script1", dummyScript("script1"), "script2", dummyScript("script2"))),
                Collections.emptyMap()
        );
        when(responseParser.parseLLMResponse("response2")).thenReturn(code2);

        final DummyLlmApi llmApi =  new DummyLlmApi(List.of("response1", "response2"));
        final ScratchLlm llm = new ScratchLlm(llmApi, new DefaultPrompts(), responseParser);

        final ParsedLlmResponseCode response = llm.singleQueryWithCodeOnlyResponse("");
        assertThat(response).isEqualTo(code2);

        final int lastIdx = llmApi.getRequests().size() - 1;
        assertThat(llmApi.getRequests().get(lastIdx)).contains("""
                /Sprite: actor1
                //Script: script2
                invalid2
                //Script: script1
                invalid
                """.stripIndent());
    }

    private ScratchLlm buildLlm(final List<String> apiResponses) {
        return new ScratchLlm(new DummyLlmApi(apiResponses), new DefaultPrompts());
    }

    private String buildScript(final String actor, final String scriptId) {
        return """
                //Sprite: %s
                //Script: %s
                when green flag clicked
                %s
                """.formatted(actor, scriptId, scriptId);
    }

    private Script dummyScript(final String scriptId) {
        return new Script(
                new GreenFlag(new NoBlockMetadata()),
                new StmtList(new CallStmt(
                        new StrId(scriptId),
                        new ExpressionList(Collections.emptyList()),
                        new NoBlockMetadata()
                ))
        );
    }
}
