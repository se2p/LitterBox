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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ComparingLiterals;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.InitialisationOfPosition;
import de.uni_passau.fim.se2.litterbox.analytics.questions.IfThenStatementExecution;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptyProject;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.Conversation;
import de.uni_passau.fim.se2.litterbox.llm.DummyLlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class LLMIssueFalsePositiveFilterTest implements JsonTest {

    @Test
    void shouldIgnorePerfumes() throws ParsingException, IOException {
        final Program program = getAST("src/test/fixtures/goodPractice/initLocInBlock.json");
        final Set<Issue> issues = new InitialisationOfPosition().check(program);
        shouldIgnoreIssues(program, issues);
    }

    @Test
    void shouldIgnoreQuestions() throws ParsingException, IOException {
        final Program program = getAST("src/test/fixtures/questions/twoScriptsWithBothIfStmts.json");
        final Set<Issue> issues = new IfThenStatementExecution().check(program);
        shouldIgnoreIssues(program, issues);
    }

    @Test
    void shouldIgnoreIssuesWithoutLocation() throws ParsingException, IOException {
        final Program program = getAST("src/test/fixtures/emptyProject.json");
        final Set<Issue> issues = new EmptyProject().check(program);
        shouldIgnoreIssues(program, issues);
    }

    private void shouldIgnoreIssues(final Program program, final Set<Issue> issues) {
        final LlmApi api = spy(new DummyLlmApi());

        final LLMIssueFalsePositiveFilter filter = new LLMIssueFalsePositiveFilter(
                api, LlmPromptProvider.get(), new QueryTarget.ProgramTarget()
        );

        final Set<Issue> issuesAfterFilter = filter.apply(program, issues);

        assertThat(issuesAfterFilter).containsExactlyElementsIn(issues);
        verify(api, never()).query(anyString());
        verify(api, never()).query(any(Conversation.class));
        verify(api, never()).query(anyString(), anyString());
    }

    @Test
    void shouldIgnoreFalsePositive() throws ParsingException, IOException {
        final Program program = getAST("src/test/fixtures/bugpattern/comparingLiterals.json");
        final Set<Issue> issues = new ComparingLiterals().check(program);
        assertThat(issues).hasSize(3);

        final LlmApi api = new DummyLlmApi("yes", "no", "yes");

        final LLMIssueFalsePositiveFilter filter = new LLMIssueFalsePositiveFilter(
                api, LlmPromptProvider.get(), new QueryTarget.ProgramTarget()
        );

        final Set<Issue> issuesAfterFilter = filter.apply(program, issues);
        assertThat(issuesAfterFilter).hasSize(2);
    }

}
