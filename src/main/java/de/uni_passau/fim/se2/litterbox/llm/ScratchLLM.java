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
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;

import java.util.Set;

public class ScratchLLM<A extends LlmApi, P extends PromptBuilder> {

    private final A llmApi;

    private final P promptBuilder;

    public ScratchLLM(final A llmApi, final P promptBuilder) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
    }

    public String askAbout(Program program, String question) {
        final String prompt = promptBuilder.askQuestion(program, question);
        return llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
    }

    public String askAbout(Program program, String spriteName, String question) {
        final String prompt = promptBuilder.askQuestion(program, spriteName, question);
        return llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
    }

    public String improve(Program program, String detectors, boolean ignoreLooseBlocks) {
        final ProgramBugAnalyzer bugAnalyzer = new ProgramBugAnalyzer(detectors, ignoreLooseBlocks);
        final Set<Issue> issues = bugAnalyzer.analyze(program);

        final String prompt = promptBuilder.improveCode(program, issues);
        final Conversation response = llmApi.query(promptBuilder.systemPrompt(), prompt);

        // TODO: Parse stuff back to program and return actual Program rather than text
        // TODO: Ask LLM to fix the ScratchBlocks output if it cannot be parsed?
        // TODO: Check how many of the issues are actually fixed

        return response.getLast().text();
    }

    // TODO: methods to continue a conversation

    // TODO: autoComplete(Program program, ScriptEntity script) ?
}
