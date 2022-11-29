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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.ActorNameNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

class GgnnGraphAnalyzerTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testProduceOutput(boolean toDotGraph, @TempDir Path outputDir) throws IOException {
        MLPreprocessorCommonOptions commonOptions = new MLPreprocessorCommonOptions(
                Path.of("src/test/fixtures/multipleSprites.json"),
                MLOutputPath.directory(outputDir),
                false, true, false, false, true,
                ActorNameNormalizer.getDefault()
        );
        GgnnGraphAnalyzer analyzer = new GgnnGraphAnalyzer(commonOptions, toDotGraph, null);
        analyzer.analyzeFile();

        Path expectedOutputFile = outputDir.resolve(expectedOutputFilename("multipleSprites", toDotGraph));
        assertThat(expectedOutputFile.toFile().exists()).isTrue();

        List<String> output = Files.readAllLines(expectedOutputFile);
        if (!toDotGraph) {
            assertThat(output).hasSize(3);
        } else {
            assertThat(output.get(0)).startsWith("digraph");
        }
    }

    @Test
    void testInvalidInput() {
        MLPreprocessorCommonOptions commonOptions = new MLPreprocessorCommonOptions(
                Path.of(""), MLOutputPath.console(), false, false, false, true, false, ActorNameNormalizer.getDefault()
        );
        GgnnGraphAnalyzer analyzer = new GgnnGraphAnalyzer(commonOptions, false, null);

        Stream<String> result = analyzer.process(Path.of("non_existing").toFile());
        assertThat(result.collect(Collectors.toList())).isEmpty();
    }

    private String expectedOutputFilename(String inputFile, boolean dotGraph) {
        return String.format("GraphData_%s.%s", inputFile, dotGraph ? "dot" : "jsonl");
    }
}
