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
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.Set;

public class LLMProgramImprovementAnalyzer extends LLMProgramModificationAnalyzer {

    private final String detectors;

    private final Set<Issue> issues;

    public LLMProgramImprovementAnalyzer(IssueTranslator translator, QueryTarget target, Set<Issue> issues) {
        super(translator, target, false);

        this.detectors = null;
        this.issues = issues;
    }

    public LLMProgramImprovementAnalyzer(
            LlmApi llmApi,
            PromptBuilder promptBuilder,
            QueryTarget target,
            Set<Issue> issues
    ) {
        super(llmApi, promptBuilder, target, false);

        this.detectors = null;
        this.issues = issues;
    }

    public LLMProgramImprovementAnalyzer(
            IssueTranslator translator,
            QueryTarget target,
            String detectors,
            boolean ignoreLooseBlocks
    ) {
        super(translator, target, ignoreLooseBlocks);

        this.detectors = detectors;
        this.issues = null;
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
        this.issues = null;
    }

    @Override
    public String buildPrompt(Program program) {
        final Set<Issue> issues;

        if (this.issues == null) {
            final ProgramBugAnalyzer bugAnalyzer = new ProgramBugAnalyzer(detectors, ignoreLooseBlocks);
            issues = bugAnalyzer.analyze(program);
        } else {
            issues = this.issues;
        }

        return promptBuilder.improveCode(program, target, issues);
    }
}
