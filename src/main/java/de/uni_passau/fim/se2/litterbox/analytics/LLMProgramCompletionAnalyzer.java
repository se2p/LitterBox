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

package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiUtils;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.util.logging.Logger;

public class LLMProgramCompletionAnalyzer extends LLMProgramModificationAnalyzer {

    private static final Logger log = Logger.getLogger(LLMProgramCompletionAnalyzer.class.getName());

    public LLMProgramCompletionAnalyzer(
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        super(target, ignoreLooseBlocks);
    }

    public LLMProgramCompletionAnalyzer(
            LlmApi llmApi,
            PromptBuilder promptBuilder,
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        super(llmApi, promptBuilder, target, ignoreLooseBlocks);
    }

    @Override
    public String callLLM(Program program) {
        final String prompt = promptBuilder.completeCode(program, target);
        log.info("Prompt: " + prompt);
        String response = llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text();
        log.info("Response: " + response);
        return LlmApiUtils.fixCommonScratchBlocksIssues(response);
    }
}
