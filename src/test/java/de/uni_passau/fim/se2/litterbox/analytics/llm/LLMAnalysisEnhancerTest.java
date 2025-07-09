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
import de.uni_passau.fim.se2.litterbox.analytics.IssueParser;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingEraseAll;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.llm.DummyLlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class LLMAnalysisEnhancerTest implements JsonTest {

    @Test
    void enhanceAnalysisNoOp(@TempDir final Path outputDir) throws ParsingException, IOException {
        final Path fixture = Path.of("src/test/fixtures/bugpattern/missingEraseAll.json");
        final Path resultPath = outputDir.resolve("missingEraseAll.json");

        final Program program = getAST(fixture.toString());
        final Set<Issue> issues = new MissingEraseAll().check(program);
        assertThat(issues).isNotEmpty();

        final DummyLlmApi api = new DummyLlmApi();
        final LLMAnalysisEnhancer enhancer = new LLMAnalysisEnhancer(
                api, LlmPromptProvider.get(), new QueryTarget.ProgramTarget(), resultPath, "all", false
        );

        enhancer.writeResultToFile(fixture, program, issues);

        final List<IssueDTO> processedIssues = parseIssues(resultPath);
        assertThat(processedIssues).hasSize(issues.size());
    }

    @Test
    void enhanceAnalysis(@TempDir final Path outputDir) throws ParsingException, IOException {
        final Path fixture = Path.of("src/test/fixtures/bugpattern/missingEraseAll.json");
        final Path resultPath = outputDir.resolve("missingEraseAll.json");

        final Program program = getAST(fixture.toString());
        final Set<Issue> issues = new MissingEraseAll().check(program);
        assertThat(issues).isNotEmpty();

        final DummyLlmApi api = new DummyLlmApi(
                "hint extension that explains the effect",
                """
                    New Finding #123:
                    Finding Description: description of the new issue
                    Finding Location: jR)UF^3guq0Z7^)#KfHN
                    """
        );
        final LLMAnalysisEnhancer enhancer = new LLMAnalysisEnhancer(
                api, LlmPromptProvider.get(), new QueryTarget.ProgramTarget(), resultPath, "all", false
        );
        enhancer.addIssueProcessor(
                new LLMIssueEffectExplainer(api, LlmPromptProvider.get(), new QueryTarget.ProgramTarget())
        );
        enhancer.addIssueProcessor(
                new LLMIssueBugExtender(api, LlmPromptProvider.get(), new QueryTarget.ProgramTarget())
        );

        enhancer.writeResultToFile(fixture, program, issues);

        final List<IssueDTO> processedIssues = parseIssues(resultPath);
        assertThat(processedIssues).hasSize(issues.size() + 1);

        final List<IssueDTO> originalIssues = processedIssues.stream()
                .filter(issue -> MissingEraseAll.NAME.equals(issue.finder()))
                .toList();
        assertThat(originalIssues).hasSize(issues.size());
        assertAll(originalIssues.stream()
                .map(issue ->
                        () -> assertThat(issue.hint()).contains("hint extension that explains the effect")
                )
        );

        final List<IssueDTO> newlyAddedIssues = processedIssues.stream()
                .filter(issue -> LLMIssueFinder.NAME.equals(issue.finder()))
                .toList();
        assertThat(newlyAddedIssues).hasSize(1);
        assertThat(newlyAddedIssues.get(0).hint()).isEqualTo("description of the new issue");
    }

    private List<IssueDTO> parseIssues(final Path issuesFile) throws ParsingException, IOException {
        return new IssueParser()
                .getIssuesPerFinder(issuesFile.toFile())
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();
    }

}
