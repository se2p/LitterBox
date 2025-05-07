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

import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;

import java.util.logging.Logger;

public class ScratchLlm {

    private static final Logger log = Logger.getLogger(ScratchLlm.class.getName());

    private final LlmApi llmApi;

    private final PromptBuilder promptBuilder;

    private final LlmResponseParser responseParser = new LlmResponseParser();

    public ScratchLlm(final LlmApi llmApi, final PromptBuilder promptBuilder) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
    }

    public ScratchLlm() {
        this(LlmApiProvider.get(), LlmPromptProvider.get());
    }

    /**
     * Prompts the LLM once and parses the response as ScratchBlocks code.
     *
     * <p>Assumes the whole output is ScratchBlocks code.
     *
     * @param prompt The prompt to query the LLM with.
     * @return The parsed response.
     */
    public ParsedLlmResponseCode singleQueryWithCodeOnlyResponse(final String prompt) {
        log.info("Prompt: " + prompt);
        final String response = llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
        log.info("Response: " + response);
        // todo: iterate and ask LLM to fix syntax if not parseable here?
        return responseParser.parseLLMResponse(response);
    }

    /**
     * Prompts the LLM once and returns the response.
     *
     * <p>Also tries to fix common ScratchBlocks issues in the response.
     *
     * @param prompt The prompt to query the LLM with.
     * @return The response.
     */
    public String singleQueryWithTextResponse(final String prompt) {
        log.info("Prompt: " + prompt);
        final String response = llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
        log.info("Response: " + response);
        return LlmResponseParser.fixCommonScratchBlocksIssues(response);
    }

}
