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
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.BoolExpression;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.llm.DummyLlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class LLMIssueBugExtenderTest implements JsonTest {

    @Test
    void doesNotAddPerfumesToPrompt() throws ParsingException, IOException {
        final Program program = getAST("src/test/fixtures/goodPractice/boolExpressions.json");
        final Set<Issue> issues = new BoolExpression().check(program);
        final DummyLlmApi api = new DummyLlmApi("no new issues");

        final LLMIssueBugExtender extender = new LLMIssueBugExtender(
                api, LlmPromptProvider.get(), new QueryTarget.ProgramTarget()
        );

        final Set<Issue> newIssueSet = extender.apply(program, issues);
        assertThat(newIssueSet).containsExactlyElementsIn(issues);

        assertThat(api.getRequests().getFirst()).doesNotContain("Issue #");
    }

    @ParameterizedTest
    @ValueSource(strings = {ScratchBlocksVisitor.SCRIPT_ID_MARKER, "", "    "})
    void addsNewIssue(final String scriptIdPrefix) throws ParsingException, IOException {
        final List<Issue> llmIssues = getLlmIssues("""
                found new issues, here is a list in the requested format

                New Finding 3:
                - Finding Description: dummy description
                spanning multiple lines
                Finding Location: %s!__9vv+fY~nCdi%%W9X!K
                """.formatted(scriptIdPrefix));
        assertThat(llmIssues).hasSize(1);

        final Issue llmIssue = llmIssues.getFirst();
        assertThat(llmIssue.getHintText()).isEqualTo("dummy description\nspanning multiple lines");
        assertThat(llmIssue.getCodeLocation()).isNotNull();
    }

    @Test
    void addsNewIssueMissingFindingNumber() throws ParsingException, IOException {
        final List<Issue> llmIssues = getLlmIssues("""
                found new issues, here is a list in the requested format

                New Finding:
                - Finding Description: dummy description
                Finding Location: //Script: !__9vv+fY~nCdi%W9X!K
                """);
        assertThat(llmIssues).hasSize(1);
    }

    @Test
    void noIssueMissingLocation() throws ParsingException, IOException {
        final List<Issue> llmIssues = getLlmIssues("""
                New Finding:
                - Finding Description: dummy description
                Finding Location: invalid
                """);
        assertThat(llmIssues).isEmpty();
    }

    @Test
    void noIssueMissingLocationMarker() throws ParsingException, IOException {
        final List<Issue> llmIssues = getLlmIssues("""
                found new issues, here is a list in the requested format

                New Finding 5456:
                - Finding Description: dummy description
                """);
        assertThat(llmIssues).isEmpty();
    }

    private List<Issue> getLlmIssues(final String llmResponse) throws ParsingException, IOException {
        final Program program = getAST("src/test/fixtures/bugpattern/comparingLiterals.json");
        final Set<Issue> issues = new ComparingLiterals().check(program);
        final LlmApi api = new DummyLlmApi(llmResponse);

        final LLMIssueBugExtender extender = new LLMIssueBugExtender(
                api, LlmPromptProvider.get(), new QueryTarget.ProgramTarget()
        );

        final Set<Issue> newIssueSet = extender.apply(program, issues);

        return newIssueSet.stream().filter(issue -> issue.getFinder() instanceof LLMIssueFinder).toList();
    }
}
