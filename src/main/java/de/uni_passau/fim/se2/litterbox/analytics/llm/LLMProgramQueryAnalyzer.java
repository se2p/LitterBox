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

import de.uni_passau.fim.se2.litterbox.analytics.ProgramAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;

import java.util.logging.Logger;

public class LLMProgramQueryAnalyzer implements ProgramAnalyzer<String> {

    private static final Logger log = Logger.getLogger(LLMProgramQueryAnalyzer.class.getName());

    private LlmApi llmApi;

    private PromptBuilder promptBuilder;

    private LlmQuery query;

    private QueryTarget target;

    // TODO: Handle this option
    private boolean ignoreLooseBlocks;

    public LLMProgramQueryAnalyzer(
            LlmApi llmApi,
            PromptBuilder promptBuilder,
            LlmQuery query,
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
        this.query = query;
        this.target = target;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    public LLMProgramQueryAnalyzer(
            LlmQuery query,
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        this(LlmApiProvider.get(), LlmPromptProvider.get(), query, target, ignoreLooseBlocks);
    }

    @Override
    public String analyze(Program program) {
        final String prompt = promptBuilder.askQuestion(program, target, query);
        log.fine("Prompt: " + prompt);
        String response = llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
        log.fine("Response: " + response);
        return response;
    }
}
