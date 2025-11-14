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
import de.uni_passau.fim.se2.litterbox.llm.ScratchLlm;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

public class LLMProgramQueryAnalyzer implements ProgramAnalyzer<String> {

    private final ScratchLlm scratchLlm;

    private final PromptBuilder promptBuilder;

    private final LlmQuery query;

    private final QueryTarget target;

    private final boolean ignoreLooseBlocks;

    public LLMProgramQueryAnalyzer(
            LlmApi llmApi,
            PromptBuilder promptBuilder,
            LlmQuery query,
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        this.promptBuilder = promptBuilder;
        this.scratchLlm = new ScratchLlm(llmApi, promptBuilder);
        this.query = query;
        this.target = target;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    public LLMProgramQueryAnalyzer(
            IssueTranslator translator,
            LlmQuery query,
            QueryTarget target,
            boolean ignoreLooseBlocks
    ) {
        this(LlmApiProvider.get(), LlmPromptProvider.get(translator), query, target, ignoreLooseBlocks);
    }

    @Override
    public String analyze(Program program) {
        final String prompt = promptBuilder.askQuestion(program, target, query, ignoreLooseBlocks);
        return scratchLlm.singleQueryWithTextResponse(prompt);
    }
}
