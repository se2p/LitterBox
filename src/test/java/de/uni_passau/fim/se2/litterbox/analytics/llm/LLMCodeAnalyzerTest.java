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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeFilteringVisitor;
import de.uni_passau.fim.se2.litterbox.llm.DummyLlmApi;
import de.uni_passau.fim.se2.litterbox.llm.prompts.LlmPromptProvider;
import de.uni_passau.fim.se2.litterbox.llm.prompts.QueryTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class LLMCodeAnalyzerTest {

    @Test
    void testAnalyzeModifyProgram(@TempDir final Path outputDir) throws IOException, ParsingException {
        final Path inputFile = Path.of("src/test/fixtures/bugpattern/equalsCondition.json");
        final Path outputFile = outputDir.resolve("equalsCondition_llm.json");

        DummyLlmApi llmApi = new DummyLlmApi("dummy response");

        LLMProgramImprovementAnalyzer improvementAnalyzer = new LLMProgramImprovementAnalyzer(
                llmApi, LlmPromptProvider.get(), new QueryTarget.ProgramTarget(), "bugs", false
        );
        LLMCodeAnalyzer analyzer = new LLMCodeAnalyzer(improvementAnalyzer, outputDir, false);

        analyzer.checkAndWrite(inputFile.toFile());

        assertThat(llmApi.getRequests().getFirst()).contains("[b]Problem:[/b]");

        final Program inputProgram = new Scratch3Parser().parseFile(inputFile.toFile());
        final Program outputProgram = new Scratch3Parser().parseFile(outputFile.toFile());
        assertThat(StructuralEquality.areStructurallyEqual(inputProgram, outputProgram)).isTrue();
    }

    @Test
    void testAnalyzeCompleteProgram(@TempDir final Path outputDir) throws IOException, ParsingException {
        final Path inputFile = Path.of("src/test/fixtures/emptyProject.sb3");
        final Path outputFile = outputDir.resolve("emptyProject_llm.sb3");

        DummyLlmApi llmApi = new DummyLlmApi("""
                //Sprite: Stage
                //Script: new-id
                when green flag clicked
                move (10) steps
                """);

        LLMProgramCompletionAnalyzer improvementAnalyzer = new LLMProgramCompletionAnalyzer(
                llmApi, LlmPromptProvider.get(), new QueryTarget.ProgramTarget(), false
        );
        LLMCodeAnalyzer analyzer = new LLMCodeAnalyzer(improvementAnalyzer, outputDir, false);

        analyzer.checkAndWrite(inputFile.toFile());

        assertThat(llmApi.getRequests().getFirst()).doesNotContain("[b]Problem:[/b]");

        final Program inputProgram = new Scratch3Parser().parseFile(inputFile.toFile());
        final Program outputProgram = new Scratch3Parser().parseFile(outputFile.toFile());

        assertThat(StructuralEquality.areStructurallyEqual(inputProgram, outputProgram)).isFalse();

        final List<Script> scripts = NodeFilteringVisitor.getBlocks(outputProgram, Script.class);
        assertThat(scripts).hasSize(1);
        assertThat(scripts.getFirst().getEvent()).isInstanceOf(GreenFlag.class);
        assertThat(scripts.getFirst().getStmtList().getStmts()).isNotEmpty();
    }
}
