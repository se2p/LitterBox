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

public class LLMIssuePerfumeExtender extends LLMIssueExtender {

    public LLMIssuePerfumeExtender(LlmApi llmApi, PromptBuilder promptBuilder, QueryTarget target) {
        super(llmApi, promptBuilder, target);
    }

    public LLMIssuePerfumeExtender(QueryTarget target) {
        super(target);
    }

    @Override
    public Set<Issue> apply(Program program, Set<Issue> issues) {
        final List<Issue> perfumes = issues.stream()
                .filter(i -> IssueType.PERFUME.equals(i.getIssueType()))
                .toList();

        final StringBuilder issueList = new StringBuilder();
        int numIssue = 0;

        for (final Issue issue : perfumes) {
            issueList
                    .append("Issue #").append(numIssue++).append('\n')
                    .append(issue.getHintText())
                    .append('\n');
        }

        final LlmQuery issueQuery = new LlmQuery.CustomQuery(promptBuilder.findNewPerfumes(issueList.toString()));
        return apply(program, issues, issueQuery, new LLMIssueFinder(IssueType.PERFUME));
    }
}
