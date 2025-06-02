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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramBugAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingCloneCall;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.DummyLlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.DefaultPrompts;
import de.uni_passau.fim.se2.litterbox.llm.prompts.PromptBuilder;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class LLMIssueFixProcessorTest implements JsonTest {

    private final PromptBuilder promptBuilder = new DefaultPrompts();

    @Test
    void ignorePerfumes() throws Exception {
        final Program program = getAST("./src/test/fixtures/goodPractice/usedVariable.json");
        final Set<Issue> issues = getIssues(program).stream()
                .filter(issue -> IssueType.PERFUME.equals(issue.getIssueType()))
                .collect(Collectors.toSet());
        assertThat(issues).isNotEmpty();

        final LlmApi api = new DummyLlmApi();
        final LLMIssueFixProcessor processor = new LLMIssueFixProcessor(
                api, promptBuilder, new QueryTarget.ProgramTarget()
        );
        final Set<Issue> processedIssues = processor.apply(program, issues);

        assertThat(processedIssues).containsExactlyElementsIn(issues);
    }

    @Test
    void ignoreNonScriptIssues() throws Exception {
        final Program program = getAST("./src/test/fixtures/goodPractice/usedVariable.json");
        final Set<Issue> issues = getIssues(program).stream()
                .filter(issue -> !IssueType.PERFUME.equals(issue.getIssueType()))
                .collect(Collectors.toSet());
        assertThat(issues).isNotEmpty();
        assertAll(issues.stream().map(issue -> () -> assertThat(issue.getScript()).isNull()));

        final LlmApi api = new DummyLlmApi();
        final LLMIssueFixProcessor processor = new LLMIssueFixProcessor(
                api, promptBuilder, new QueryTarget.ProgramTarget()
        );
        final Set<Issue> processedIssues = processor.apply(program, issues);

        assertThat(processedIssues).containsExactlyElementsIn(issues);
    }

    @Test
    void processScriptIssues() throws Exception {
        final Program program = getAST("./src/test/fixtures/bugpattern/missingCloneCall.json");
        final Set<Issue> issues = getIssues(program).stream()
                .filter(issue -> issue.getScript() != null)
                .filter(issue -> MissingCloneCall.NAME.equals(issue.getFinderName()))
                .collect(Collectors.toSet());
        assertThat(issues).hasSize(1);

        final LlmApi api = new DummyLlmApi("""
                //Sprite: Sprite1
                //Script: 7_}Und/.Cm%tEi*_L~DK
                when I start as a clone
                wait (1) seconds
                create clone of (myself v)
                """);
        final LLMIssueFixProcessor processor = new LLMIssueFixProcessor(
                api, promptBuilder, new QueryTarget.ProgramTarget()
        );
        final Set<Issue> processedIssues = processor.apply(program, issues);

        assertThat(processedIssues).hasSize(issues.size());

        final Issue updatedIssue = processedIssues.stream().findAny().orElseThrow();
        assertThat(updatedIssue.getRefactoredScriptOrProcedureDefinition()).isNotNull();
    }

    @Test
    void processScriptIssuesNotFoundInResponse() throws Exception {
        final Program program = getAST("./src/test/fixtures/bugpattern/missingCloneCall.json");
        final Set<Issue> issues = getIssues(program).stream()
                .filter(issue -> issue.getScript() != null)
                .filter(issue -> MissingCloneCall.NAME.equals(issue.getFinderName()))
                .collect(Collectors.toSet());
        assertThat(issues).hasSize(1);
        final Issue originalIssue = issues.stream().findFirst().orElseThrow();

        final LlmApi api = new DummyLlmApi("""
                //Sprite: Sprite1
                //Script: newIdNotInOriginalProgram
                when I start as a clone
                wait (1) seconds
                """);
        final LLMIssueFixProcessor processor = new LLMIssueFixProcessor(
                api, promptBuilder, new QueryTarget.ProgramTarget()
        );
        final Set<Issue> processedIssues = processor.apply(program, issues);

        assertThat(processedIssues).hasSize(issues.size());

        final Issue updatedIssue = processedIssues.stream().findAny().orElseThrow();
        assertThat(updatedIssue).isNotEqualTo(originalIssue);
    }

    private Set<Issue> getIssues(final Program program) {
        final ProgramBugAnalyzer analyzer = new ProgramBugAnalyzer("all", false);
        return analyzer.analyze(program);
    }
}
