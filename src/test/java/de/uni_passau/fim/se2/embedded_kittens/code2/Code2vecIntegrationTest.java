/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.embedded_kittens.code2;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.uni_passau.fim.se2.embedded_kittens.CliTest;

class Code2vecIntegrationTest extends CliTest {

    @Test
    void processProgramWithMultipleSpritesToFile(@TempDir Path tempDir) throws IOException {
        commandLine.execute(
            "code2vec", "-p", "src/test/fixtures/multipleSprites.json", "-o", tempDir.toString()
        );

        final List<String> outputLines = Files.readAllLines(tempDir.resolve("multipleSprites.txt"));
        assertThat(outputLines).hasSize(3);
        assertThat(outputLines).containsExactly(
            "cat 39,625791294,hi_! 39,1493538624,Show hi_!,-547448667,Show",
            "abby GreenFlag,-2069003229,hello_!",
            "stage GreenFlag,1809747443,10"
        );
    }

    @Test
    void ensureRecursiveFileProcessingNoOverwrites(@TempDir Path input, @TempDir Path output) throws IOException {
        Files.createDirectories(input.resolve("a").resolve("b"));

        final Path fixture = Path.of("src/test/fixtures/multipleSprites.json");
        Files.copy(fixture, input.resolve(fixture.getFileName()));
        Files.copy(fixture, input.resolve("a").resolve(fixture.getFileName()));
        Files.copy(fixture, input.resolve("a").resolve("b").resolve(fixture.getFileName()));

        commandLine.execute("code2vec", "-p", input.toString(), "-o", output.toString());

        assertThat(Files.walk(output).filter(p -> p.toFile().isFile())).hasSize(3);
        assertThat(Files.walk(output).filter(p -> p.toFile().isDirectory())).hasSize(3);
    }
}
