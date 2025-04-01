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
package de.uni_passau.fim.se2.litterbox.llm.prompts;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.Collection;
import java.util.List;

public abstract class PromptBuilder {

    /**
     * The system prompt used as starting point for each conversation with an LLM.
     *
     * @return The prompt, or {@code null} if is not needed.
     */
    public String systemPrompt() {
        return null;
    }

    public abstract String askQuestion(Program program, QueryTarget target, LlmQuery question);

    public abstract String improveCode(Program program, QueryTarget target, Collection<Issue> issues);

    public abstract String completeCode(Program program, QueryTarget target);

    public abstract String createPromptForCommonQuery(CommonQuery query);

    public abstract String findNewBugs(String existingBugsDescription);

    public abstract String explainIssue(Issue issue);

    protected abstract String describeTarget(final Program program, QueryTarget target);

    protected List<String> issueTypes(final Collection<Issue> issues) {
        return issues.stream().map(Issue::getFinderName).sorted().toList();
    }

    protected List<String> issueHints(final Collection<Issue> issues) {
        return issues.stream().map(Issue::getHintText).toList();
    }
}
