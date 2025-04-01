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

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.ScratchLlm;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

public class LLMIssueEffectExplainer implements LLMIssueProcessor {
    private static final Logger log = Logger.getLogger(LLMIssueEffectExplainer.class.getName());

    protected ScratchLlm scratchLLM;

    protected LlmApi llmApi;

    protected PromptBuilder promptBuilder;

    protected QueryTarget target;

    public LLMIssueEffectExplainer(LlmApi llmApi,
                                 PromptBuilder promptBuilder,
                                 QueryTarget target) {
        this.llmApi = llmApi;
        this.promptBuilder = promptBuilder;
        this.scratchLLM = new ScratchLlm(llmApi, promptBuilder);
        this.target = target;
    }

    public LLMIssueEffectExplainer(QueryTarget target) {
        this(LlmApiProvider.get(), LlmPromptProvider.get(), target);
    }

    @Override
    public Set<Issue> apply(Program program, Set<Issue> issues) {
        Set<Issue> enhancedIssues = new LinkedHashSet<>();

        for (Issue issue : issues) {
            log.info("Current issue: " + issue.getFinderName() + " in sprite " + issue.getActorName());

            LlmQuery issueQuery = new LlmQuery.CustomQuery(promptBuilder.explainIssue(issue));
            final String prompt = promptBuilder.askQuestion(program, target, issueQuery);
            log.info("Prompt: " + prompt);
            String response = scratchLLM.singleQueryWithTextResponse(prompt);
            log.info("Response: " + response);

            // TODO: Appending the explanation to the existing hint would be undone
            //       if the LLMIssueHintProcessor is applied afterwards.
            Hint improvedHint = Hint.fromText(issue.getHintText() + "\n" + response);

            IssueBuilder issueBuilder = new IssueBuilder();
            issueBuilder.fromIssue(issue).withHint(improvedHint);
            enhancedIssues.add(issueBuilder.build());
        }
        return enhancedIssues;
    }
}
