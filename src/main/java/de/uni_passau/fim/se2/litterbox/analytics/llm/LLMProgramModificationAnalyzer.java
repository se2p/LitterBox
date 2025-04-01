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
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.llm.LlmResponseParser;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLlm;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

public abstract class LLMProgramModificationAnalyzer implements ProgramAnalyzer<Program> {

    protected ScratchLlm scratchLlm;

    protected LlmApi llmApi;

    protected PromptBuilder promptBuilder;

    protected QueryTarget target;

    protected boolean ignoreLooseBlocks;

    protected LLMProgramModificationAnalyzer(
            LlmApi llmApi,
            PromptBuilder promptBuilder,
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
        this.scratchLlm = new ScratchLlm(llmApi, promptBuilder);
        this.target = target;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    protected LLMProgramModificationAnalyzer(
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        this(LlmApiProvider.get(), LlmPromptProvider.get(), target, ignoreLooseBlocks);
    }

    public abstract String callLLM(Program program);

    @Override
    public Program analyze(Program program) {
        String response = callLLM(program);

        LlmResponseParser responseParser = new LlmResponseParser();
        return responseParser.parseResultAndUpdateProgram(program, response);
    }
}
