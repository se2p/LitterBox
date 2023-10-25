/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model.StatementTreeSequence;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.ActorNameNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

class AstnnAnalyzerTest {
    @Test
    void testEmptyResultOnInvalidProgram() {
        final Stream<StatementTreeSequence> result = processFixture(
                "astnn_definitely_non_existing.json", true, true, false
        );
        assertThat(result.count()).isEqualTo(0);
    }

    @Test
    void testIgnoreEmptySprites() {
        // one actor with an empty name, another one with no blocks
        final Stream<StatementTreeSequence> result = processFixture(
                "ml_preprocessing/astnn/empty_actors.json", false, true, false
        );
        assertThat(result.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testFilterSpritesDefaultName(boolean includeDefaultSprites) {
        final Stream<StatementTreeSequence> result = processFixture(
                "allBlocks.json", true, includeDefaultSprites, false
        );

        final List<String> actors = result.map(StatementTreeSequence::label).toList();
        assertThat(actors).containsAtLeast("stage", "ball");
        if (includeDefaultSprites) {
            assertThat(actors).contains("sprite");
        } else {
            assertThat(actors).doesNotContain("sprite");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testStageIncludedPerSprite(boolean includeStage) {
        final Stream<StatementTreeSequence> result = processFixture(
                "multipleSprites.json", includeStage, true, false
        );

        int expectedItems = includeStage ? 3 : 2;
        assertThat(result.count()).isEqualTo(expectedItems);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testStageIncludedWholeProgram(boolean includeStage) {
        final Stream<StatementTreeSequence> result = processFixture(
                "multipleSprites.json", includeStage, true, true
        );

        assertThat(result.count()).isEqualTo(1);
    }

    @Test
    void testWriteToFile(@TempDir Path outputDir) throws Exception {
        final MLPreprocessorCommonOptions options = new MLPreprocessorCommonOptions(
                Path.of("src/test/fixtures/multipleSprites.json"),
                MLOutputPath.directory(outputDir),
                false,
                false,
                false,
                true,
                false,
                ActorNameNormalizer.getDefault()
        );

        final AstnnAnalyzer analyzer = new AstnnAnalyzer(options);
        analyzer.analyzeFile();

        final Path expectedOutputFile = outputDir.resolve("multipleSprites.jsonl");
        assertThat(expectedOutputFile.toFile().exists()).isTrue();

        final List<String> outputContent = Files.readAllLines(expectedOutputFile);
        assertThat(outputContent).hasSize(2);
    }

    private Stream<StatementTreeSequence> processFixture(
            final String program, boolean includeStage, boolean includeDefaultSprites, boolean wholeProgram
    ) {
        final Path programPath = Path.of("src/test/fixtures").resolve(program);
        final AstnnAnalyzer analyzer = new AstnnAnalyzer(
                options(programPath, includeStage, includeDefaultSprites, wholeProgram)
        );
        return analyzer.check(programPath.toFile());
    }

    private MLPreprocessorCommonOptions options(
            final Path inputPath, boolean includeStage, boolean includeDefaultSprites, boolean wholeProgram
    ) {
        return new MLPreprocessorCommonOptions(
                inputPath,
                MLOutputPath.console(),
                false,
                includeStage,
                wholeProgram,
                includeDefaultSprites,
                false,
                ActorNameNormalizer.getDefault()
        );
    }
}
