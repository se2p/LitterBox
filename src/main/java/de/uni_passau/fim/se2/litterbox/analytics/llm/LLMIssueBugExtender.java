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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmQuery;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;

import java.util.List;
import java.util.Set;

public class LLMIssueBugExtender extends LLMIssueExtender {

    public LLMIssueBugExtender(LlmApi llmApi, PromptBuilder promptBuilder, QueryTarget target) {
        super(llmApi, promptBuilder, target);
    }

    public LLMIssueBugExtender(QueryTarget target) {
        super(target);
    }

    @Override
    public Set<Issue> apply(Program program, Set<Issue> issues) {
        StringBuilder issueList = new StringBuilder();
        int numIssue = 0;
        List<Issue> bugsOrSmells = issues.stream()
                .filter(i -> IssueType.BUG.equals(i.getIssueType()) || IssueType.SMELL.equals(i.getIssueType()))
                .toList();

        for (Issue issue : bugsOrSmells) {
            issueList.append("Issue #").append(numIssue++).append("\n");
            issueList.append(issue.getHintText());
            issueList.append("\n");
        }

        LlmQuery issueQuery = new LlmQuery.CustomQuery(promptBuilder.findNewBugs(issueList.toString()));

        return apply(program, issues, issueQuery, new LLMIssueFinder(IssueType.BUG));
    }
}
