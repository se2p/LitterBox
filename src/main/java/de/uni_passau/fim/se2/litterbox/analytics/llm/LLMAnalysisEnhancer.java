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

import de.uni_passau.fim.se2.litterbox.analytics.BugAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiUtils;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

public class LLMAnalysisEnhancer extends BugAnalyzer {

    private static final Logger log = Logger.getLogger(LLMAnalysisEnhancer.class.getName());

    protected LlmApi llmApi;

    protected PromptBuilder promptBuilder;

    protected QueryTarget target;

    public LLMAnalysisEnhancer(
            LlmApi llmApi,
            PromptBuilder promptBuilder,
            QueryTarget target,
            Path output, String detectors,
            boolean ignoreLooseBlocks
    ) {
        super(output, detectors, ignoreLooseBlocks, false, false);
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
        this.target = target;
    }

    public LLMAnalysisEnhancer(
            QueryTarget target,
            Path output, String detectors,
            boolean ignoreLooseBlocks
    ) {
        super(output, detectors, ignoreLooseBlocks, false, false);
        this.llmApi = LlmApiProvider.get();
        this.promptBuilder = LlmPromptProvider.get();
        this.target = target;
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Set<Issue> result) {
        Set<Issue> enhancedResult = enhanceIssues(program, result);
        super.writeResultToFile(projectFile, program, enhancedResult);
    }

    private Set<Issue> enhanceIssues(Program program, Set<Issue> result) {
        Set<Issue> enhancedResult = new LinkedHashSet<>();
        for (Issue issue : result) {
            log.info("Current issue: " + issue.getFinderName() +" in sprite "+issue.getActorName());

            if (issue.getScript() == null) {
                // Issue does not refer to individual script, not fixing it for now
                log.info("Cannot fix issue not related to individual script.");
                enhancedResult.add(issue);
                continue;

                // TODO: sprite_naming could be treated by suggesting a new name, but how to integrate this?
            } else if (issue.getIssueType() == IssueType.PERFUME) {
                // No need to produce fixes for perfumes
                enhancedResult.add(issue);
                continue;
            }

            final String prompt = promptBuilder.improveCode(program, target, Set.of(issue));
            log.info("Prompt: " + prompt);
            String response = LlmApiUtils.fixCommonScratchBlocksIssues(llmApi.query(promptBuilder.systemPrompt(), prompt).getLast().text());
            log.info("Response: " + response);

            LLMResponseParser responseParser = new LLMResponseParser();
            Script fixedScript = responseParser.parseResultAndUpdateScript(program, issue.getScript(), response);
            log.info("Proposed refactoring: " + ScratchBlocksVisitor.of(fixedScript));

            Issue enhancedIssue = new Issue(
                    issue.getFinder(),
                    issue.getSeverity(),
                    program,
                    issue.getActor(),
                    issue.getScript(),
                    issue.getCodeLocation(),
                    issue.getCodeMetadata(),
                    issue.getHint()
            );
            enhancedIssue.setRefactoredScriptOrProcedureDefinition(fixedScript);
            enhancedResult.add(enhancedIssue);
        }
        return enhancedResult;
    }
}
