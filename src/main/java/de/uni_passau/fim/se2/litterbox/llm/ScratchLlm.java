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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    public ParsedLlmResponseCode singleQueryWithCodeOnlyResponse(final Program program, final String prompt) {
        log.info("Prompt: " + prompt);
        final String response = llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
        log.info("Response: " + response);

        final ParsedLlmResponseCode initialResponse = responseParser.parseLLMResponse(response);

        return askForSyntaxFixes(program, initialResponse, MAX_SYNTAX_FIX_TRIES);
    }

    private ParsedLlmResponseCode askForSyntaxFixes(
            final Program program, final ParsedLlmResponseCode response, final int remainingTries
    ) {
        if (remainingTries <= 0) {
            return response;
        }

        final String scratchBlocksScripts = failedScriptsAsSingleScratchBlocksSnippet(program, response);
        // no more broken scripts
        if (scratchBlocksScripts.isBlank()) {
            return response;
        }

        final String rawResponse = llmApi.query(promptBuilder.fixSyntax(scratchBlocksScripts)).getLast().text();
        log.info("Requested fix for broken script:\n" + scratchBlocksScripts);
        log.info("Received fixed script:\n" + rawResponse);
        final ParsedLlmResponseCode parsedResponse = responseParser.parseLLMResponse(rawResponse);

        final ParsedLlmResponseCode newResponse = new ParsedLlmResponseCode(
                response.scripts(), Collections.emptyMap(), Collections.emptyMap()
        ).merge(parsedResponse);
        return askForSyntaxFixes(program, newResponse, remainingTries - 1);
    }

    /**
     * Converts the scripts for which parsing failed into a single new ScratchBlocks snippet.
     *
     * @param program The program for which the query was asked.
     * @param response An LLM response.
     * @return The scripts combined into a ScratchBlocks snippet that can be read back by our parser.
     */
    private String failedScriptsAsSingleScratchBlocksSnippet(
            final Program program, final ParsedLlmResponseCode response
    ) {
        final String sbParseFailedScripts = scriptsAsScratchBlocksSnippet(
                response.parseFailedScripts(), Function.identity()
        );

        // We parse broken lines as customBlockCallStmts in most cases instead of failing, so usually our parser
        // silently converts the broken syntax to something representable in the LitterBox AST without throwing an
        // error. Those cases would then have to be fixed by the user manually.
        // Since custom blocks can have any arbitrary name, we would have to add something like a ‘strict’ parser
        // option that only allows custom block calls for custom blocks which already exist in the project to catch
        // such occurrences.
        final var scriptsWithSyntaxErrors = findScriptsWithUndefinedCallStmts(program, response.scripts());
        final String sbScriptsWithSyntaxErrors = scriptsAsScratchBlocksSnippet(
                scriptsWithSyntaxErrors, ScratchBlocksVisitor::of
        );

        final StringBuilder result = new StringBuilder();
        result.append(sbParseFailedScripts);
        if (!result.isEmpty()) {
            result.append("\n\n");
        }
        result.append(sbScriptsWithSyntaxErrors);

        return result.toString();
    }

    private Map<String, Map<String, ScriptEntity>> findScriptsWithUndefinedCallStmts(
            final Program program, final Map<String, Map<String, ScriptEntity>> scripts
    ) {
        final Map<String, Map<String, ScriptEntity>> result = new HashMap<>();

        for (final var actorEntry : scripts.entrySet()) {
            final String actorName = actorEntry.getKey();
            final Map<String, ScriptEntity> actorMap = new HashMap<>();

            for (final var scriptEntry : actorEntry.getValue().entrySet()) {
                if (containsUndefinedCallStmts(program, actorName, scriptEntry.getValue())) {
                    actorMap.put(scriptEntry.getKey(), scriptEntry.getValue());
                }
            }

            if (!actorMap.isEmpty()) {
                result.put(actorName, actorMap);
            }
        }

        return result;
    }

    private boolean containsUndefinedCallStmts(final Program program, final String actor, final ScriptEntity script) {
        return NodeFilteringVisitor.getBlocks(script, CallStmt.class).stream()
                .map(callStmt -> program
                        .getProcedureMapping()
                        .getProcedureForName(actor, callStmt.getIdent().getName())
                )
                .anyMatch(Optional::isEmpty);
    }

    private <T> String scriptsAsScratchBlocksSnippet(
            final Map<String, Map<String, T>> scripts, final Function<T, String> scriptToScratchBlocks
    ) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (final var actorEntry : scripts.entrySet()) {
            stringBuilder.append("//Sprite: ").append(actorEntry.getKey()).append('\n');
            for (final var scriptEntry : actorEntry.getValue().entrySet()) {
                final String scriptScratchBlocks = scriptToScratchBlocks.apply(scriptEntry.getValue());
                stringBuilder
                        .append("//Script: ").append(scriptEntry.getKey()).append('\n')
                        .append(scriptScratchBlocks).append('\n');
            }
        }

        return stringBuilder.toString().trim();
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
