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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramBugAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.Conversation;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiUtils;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.util.Set;
import java.util.logging.Logger;

public class LLMProgramImprovementAnalyzer extends LLMProgramModificationAnalyzer {

    private static final Logger log = Logger.getLogger(LLMProgramImprovementAnalyzer.class.getName());

    private String detectors;

    public LLMProgramImprovementAnalyzer(
            QueryTarget target,
            String detectors,
            boolean ignoreLooseBlocks
    ) {
        super(target, ignoreLooseBlocks);
        this.detectors = detectors;
    }

    public LLMProgramImprovementAnalyzer(
            LlmApi llmApi,
            PromptBuilder promptBuilder,
            QueryTarget target,
            String detectors,
            boolean ignoreLooseBlocks
    ) {
        super(llmApi, promptBuilder, target, ignoreLooseBlocks);
        this.detectors = detectors;
    }

    @Override
    public String callLLM(Program program) {
        final ProgramBugAnalyzer bugAnalyzer = new ProgramBugAnalyzer(detectors, ignoreLooseBlocks);
        final Set<Issue> issues = bugAnalyzer.analyze(program);

        final String prompt = promptBuilder.improveCode(program, target, issues);
        log.info("Prompt: " + prompt);
        final Conversation response = llmApi.query(promptBuilder.systemPrompt(), prompt);
        log.info("Response: " + LlmApiUtils.fixCommonScratchBlocksIssues(response.getLast().text()));

        return LlmApiUtils.fixCommonScratchBlocksIssues(response.getLast().text());
    }
}
