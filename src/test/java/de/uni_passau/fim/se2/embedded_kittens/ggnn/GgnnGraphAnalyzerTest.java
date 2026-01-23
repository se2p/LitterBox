/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens.ggnn;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.uni_passau.fim.se2.embedded_kittens.MLOutputPath;
import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;

class GgnnGraphAnalyzerTest {

    @ParameterizedTest
    @EnumSource(GgnnOutputFormat.class)
    void testProduceOutput(GgnnOutputFormat outputFormat, @TempDir Path outputDir) throws IOException {
        MLPreprocessorCommonOptions commonOptions = new MLPreprocessorCommonOptions(
            MLOutputPath.directory(outputDir),
            true, false, true,
            ActorNameNormalizer.getDefault()
        );
        GgnnGraphPreprocessor analyzer = new GgnnGraphPreprocessor(commonOptions, outputFormat, null);
        analyzer.processPerSprite(Path.of("src/test/fixtures/multipleSprites.json"));

        Path expectedOutputFile = outputDir.resolve(expectedOutputFilename("multipleSprites", outputFormat));
        assertThat(expectedOutputFile.toFile().exists()).isTrue();

        List<String> output = Files.readAllLines(expectedOutputFile);
        if (!outputFormat.isDotGraph()) {
            assertThat(output).hasSize(3);
            assertThat(output.getFirst()).contains("\"nodeLabelMap\"");
            assertThat(output.getFirst()).contains("\"nodeTypeMap\"");
        }
        else {
            assertThat(output.getFirst()).startsWith("digraph");
        }
    }

    private String expectedOutputFilename(String inputFile, GgnnOutputFormat outputFormat) {
        return String.format("GraphData_%s.%s", inputFile, outputFormat.isDotGraph() ? "dot" : "jsonl");
    }
}
