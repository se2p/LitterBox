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

    public LLMIssueBugExtender(LlmApi llmApi,
                               PromptBuilder promptBuilder,
                               QueryTarget target) {
        super(llmApi, promptBuilder, target);
    }

    public LLMIssueBugExtender(QueryTarget target) {
        super(target);
    }

    @Override
    public Set<Issue> apply(Program program, Set<Issue> issues) {
        StringBuilder issueList = new StringBuilder();
        int numIssue = 0;
        List<Issue> bugsOrSmells = issues.stream().filter(i -> i.getIssueType() == IssueType.BUG || i.getIssueType() == IssueType.SMELL).toList();
        for (Issue issue : bugsOrSmells) {
            issueList.append("Issue #" + numIssue++ + "\n");
            issueList.append(issue.getHintText());
            issueList.append("\n");
        }

        LlmQuery issueQuery = new LlmQuery.CustomQuery("""
                    A static code analysis tool identified the following list of issues in the given code:
                    %s

                    List any further bugs in the code not already included in this list.
                    Do not suggest new program features.
                    Do not list generic issues such as "lack of comments in the code".
                    New issues must describe problems related to specific existing statements in the code.
                    Do not list bugs that are already contained in the list of issues above.
                    Report each issue using the following structure:

                    New Finding <number>:
                    - Finding Description: <textual issue description>
                    - Finding Location: <ID of the script containing the issue>
                    """.formatted(issueList.toString()));

        return apply(program, issues, issueQuery, new LLMIssueFinder(IssueType.BUG));
    }
}
