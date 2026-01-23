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
package de.uni_passau.fim.se2.embedded_kittens.llm;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.embedded_kittens.CliTest;

class LlmCliTest extends CliTest {

    @Test
    void buildProgramScratchBlocks() {
        commandLine.execute("llm", "-p", "src/test/fixtures/multipleSprites.json", "--whole-program");
        assertEmptyStdErr();

        final List<String> outputLines = getOutput().lines().toList();
        assertThat(outputLines).hasSize(1);
        assertThat(outputLines.getFirst()).contains("//Sprite: Stage");
        assertThat(outputLines.getFirst()).contains("//Sprite: abby");
        assertThat(outputLines.getFirst()).contains("when green flag clicked");
    }

    @Test
    void buildProgramSpritesScratchBlocks() {
        commandLine.execute("llm", "-p", "src/test/fixtures/multipleSprites.json");
        assertEmptyStdErr();

        final List<String> outputLines = getOutput().lines().toList();
        assertThat(outputLines).hasSize(3);
        assertThat(outputLines).containsExactly(
            """
                {"originalLabel":"Stage","label":"stage","program":"//Sprite: Stage\\n\\n//Script: }r3UNB}^zo[CMz4[fg-.\\nwhen green flag clicked\\nrepeat (10)\\nend\\n"}
                """
                .strip(),
            """
                {"originalLabel":"cat","label":"cat","program":"//Sprite: cat\\n\\n//Script: b^1`lb!!1@{vyh9hiru-\\nwhen [right arrow v] key pressed\\nsay [Hi!]\\nshow\\n"}
                """
                .strip(),
            """
                {"originalLabel":"abby","label":"abby","program":"//Sprite: abby\\n\\n//Script: iYhbPcaHZfN*73Kq-F.-\\nwhen green flag clicked\\nsay [Hello!]\\n"}
                """
                .strip()
        );
    }

    @Test
    void buildProgramSpritesExcludingStage() {
        commandLine.execute("llm", "-p", "src/test/fixtures/multipleSprites.json", "--exclude-stage");
        assertEmptyStdErr();

        final List<String> outputLines = getOutput().lines().toList();
        assertThat(outputLines).hasSize(2);
        assertThat(outputLines).doesNotContain("\"originalLabel\":\"Stage\"\n");
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void buildProgramSpritesExcludingDefaultNames(final boolean excludeDefaultSprites) {
        if (excludeDefaultSprites) {
            commandLine.execute(
                "llm", "-p", "src/test/fixtures/allBlocks.json", "--exclude-stage", "--exclude-default-sprites"
            );
        }
        else {
            commandLine.execute("llm", "-p", "src/test/fixtures/allBlocks.json", "--exclude-stage");
        }
        assertEmptyStdErr();

        final List<String> outputLines = getOutput().lines().toList();
        assertThat(outputLines).hasSize(excludeDefaultSprites ? 1 : 2);
        assertThat(outputLines).doesNotContain("\"originalLabel\":\"Stage\"\n");
        if (excludeDefaultSprites) {
            assertThat(outputLines).doesNotContain("\"originalLabel\":\"Sprite1\"\n");
        }
    }

    @Test
    void produceFile(@TempDir Path tempDir) {
        commandLine.execute("llm", "-p", "src/test/fixtures/multipleSprites.json", "-o", tempDir.toString());
        assertThat(tempDir.resolve("multipleSprites.jsonl").toFile().exists()).isTrue();
    }
}
