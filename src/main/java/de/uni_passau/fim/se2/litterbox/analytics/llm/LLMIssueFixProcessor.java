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
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.llm.LlmResponseParser;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLlm;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LLMIssueFixProcessor implements LLMIssueProcessor {

    private static final Logger log = Logger.getLogger(LLMIssueFixProcessor.class.getName());

    protected ScratchLlm scratchLlm;

    protected LlmApi llmApi;

    protected PromptBuilder promptBuilder;

    protected QueryTarget target;

    public LLMIssueFixProcessor(LlmApi llmApi,
                                PromptBuilder promptBuilder,
                                QueryTarget target) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
        this.scratchLlm = new ScratchLlm(llmApi, promptBuilder);
        this.target = target;
    }

    public LLMIssueFixProcessor(QueryTarget target) {
        this(LlmApiProvider.get(), LlmPromptProvider.get(), target);
    }

    @Override
    public Set<Issue> apply(Program program, Set<Issue> issues) {
        return issues.stream().map(issue -> extracted(program, issue)).collect(Collectors.toSet());
    }

    private Issue extracted(Program program, Issue issue) {
        log.info("Current issue: " + issue.getFinderName() + " in sprite " + issue.getActorName());

        if (issue.getScript() == null) {
            // Issue does not refer to individual script, not fixing it for now
            log.info("Cannot fix issue not related to individual script.");
            return issue;
        } else if (issue.getIssueType() == IssueType.PERFUME) {
            // No need to produce fixes for perfumes
            return issue;
        }

        final String prompt = promptBuilder.improveCode(program, target, Set.of(issue));
        log.info("Prompt: " + prompt);
        String response = scratchLlm.singleQueryWithTextResponse(prompt);
        log.info("Response: " + response);

        IssueBuilder issueBuilder = new IssueBuilder();
        issueBuilder.fromIssue(issue).withProgram(program);

        LlmResponseParser responseParser = new LlmResponseParser();
        Script fixedScript = responseParser.parseResultAndUpdateScript(program, issue.getScript(), response);
        if (fixedScript != null) {
            log.info("Proposed refactoring: " + ScratchBlocksVisitor.of(fixedScript));
            issueBuilder = issueBuilder.withRefactoring(fixedScript);
        }

        return issueBuilder.build();
    }
}
