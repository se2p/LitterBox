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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.OpenAiApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.DefaultPrompts;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.util.Set;
import java.util.logging.Logger;

public class ScratchLLM {

    private static final Logger log = Logger.getLogger(ScratchLLM.class.getName());

    private final LlmApi llmApi;

    private final PromptBuilder promptBuilder;

    public ScratchLLM(final LlmApi llmApi, final PromptBuilder promptBuilder) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
    }

    public String askAbout(Program program, QueryTarget target, LlmQuery question) {
        final String prompt = promptBuilder.askQuestion(program, target, question);
        log.fine("Prompt: " + prompt);
        String response = llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
        log.fine("Response: " + response);
        return response;
    }

    public String improve(Program program, QueryTarget target, String detectors, boolean ignoreLooseBlocks) {
        final ProgramBugAnalyzer bugAnalyzer = new ProgramBugAnalyzer(detectors, ignoreLooseBlocks);
        final Set<Issue> issues = bugAnalyzer.analyze(program);

        final String prompt = promptBuilder.improveCode(program, target, issues);
        log.info("Prompt: " + prompt);
        final Conversation response = llmApi.query(promptBuilder.systemPrompt(), prompt);
        log.info("Response: " + fixCommonScratchBlocksIssues(response.getLast().text()));

        return fixCommonScratchBlocksIssues(response.getLast().text());
    }

    public String autoComplete(Program program, QueryTarget target) {
        final String prompt = promptBuilder.completeCode(program, target);
        log.info("Prompt: " + prompt);
        String response = llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
        log.info("Response: " + response);
        return fixCommonScratchBlocksIssues(response);
    }

    /*
     * Try to fix common obvious errors in scratchblocks syntax produced by LLMs...
     */
    public String fixCommonScratchBlocksIssues(String scratchBlocks) {
        return scratchBlocks.replace("set rotation to", "point in direction");
    }


    public static ScratchLLM buildScratchLLM() {
        return new ScratchLLM(new OpenAiApi(), new DefaultPrompts());
    }

    // TODO: methods to continue a conversation

}
