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

import java.util.Collections;
import java.util.logging.Logger;

public class ScratchLlm {

    private static final Logger log = Logger.getLogger(ScratchLlm.class.getName());

    private static final int MAX_SYNTAX_FIX_TRIES = 1;

    private final LlmApi llmApi;

    private final PromptBuilder promptBuilder;

    private final LlmResponseParser responseParser;

    public ScratchLlm(final LlmApi llmApi, final PromptBuilder promptBuilder, final LlmResponseParser responseParser) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
    }

    public ScratchLlm(final LlmApi llmApi, final PromptBuilder promptBuilder) {
        this(llmApi, promptBuilder, new LlmResponseParser());
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

        final ParsedLlmResponseCode initialResponse = responseParser.parseLLMResponse(response);
        if (initialResponse.parseFailedScripts().isEmpty()) {
            return initialResponse;
        } else {
            // We parse broken lines as customBlockCallStmts in most cases instead of failing, so usually our parser
            // silently converts the broken syntax to something representable in the LitterBox AST without throwing an
            // error. Those cases would then have to be fixed by the user manually.
            // Since custom blocks can have any arbitrary name, we would have to add something like a ‘strict’ parser
            // option that only allows custom block calls for custom blocks which already exist in the project to catch
            // such occurrences.
            return askForSyntaxFixes(initialResponse, MAX_SYNTAX_FIX_TRIES);
        }
    }

    private ParsedLlmResponseCode askForSyntaxFixes(final ParsedLlmResponseCode response, final int remainingTries) {
        if (remainingTries <= 0) {
            return response;
        }

        final String scratchBlocksScripts = failedScriptsAsSingleScratchBlocksSnippet(response);
        final String rawResponse = llmApi.query(promptBuilder.fixSyntax(scratchBlocksScripts)).getLast().text();
        final ParsedLlmResponseCode parsedResponse = responseParser.parseLLMResponse(rawResponse);

        final ParsedLlmResponseCode newResponse = new ParsedLlmResponseCode(response.scripts(), Collections.emptyMap())
                .merge(parsedResponse);
        return askForSyntaxFixes(newResponse, remainingTries - 1);
    }

    /**
     * Converts the scripts for which parsing failed into a single new ScratchBlocks snippet.
     *
     * @param response An LLM response.
     * @return The scripts combined into a ScratchBlocks snippet that can be read back by our parser.
     */
    private String failedScriptsAsSingleScratchBlocksSnippet(final ParsedLlmResponseCode response) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (final var actorEntry : response.parseFailedScripts().entrySet()) {
            stringBuilder.append("//Sprite: ").append(actorEntry.getKey()).append('\n');
            for (final var scriptEntry : actorEntry.getValue().entrySet()) {
                stringBuilder
                        .append("//Script: ").append(scriptEntry.getKey()).append('\n')
                        .append(scriptEntry.getValue()).append('\n');
            }
        }

        return stringBuilder.toString();
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
