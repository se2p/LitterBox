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
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.CliTest;
import de.uni_passau.fim.se2.embedded_kittens.EmbeddedKittensMain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Code2seqIntegrationTest extends CliTest<EmbeddedKittensMain> {

    private static final String C2S_CMD = "code2seq";
    private static final String INPUT_FLAG = "-p";

    Code2seqIntegrationTest() {
        super(new EmbeddedKittensMain());
    }

    @Test
    void disallowBothWholeProgramAndPerSprite() {
        int returnCode = commandLine.execute(C2S_CMD, "--whole-program", "--scripts");
        assertThat(returnCode).isNotEqualTo(0);
        assertThat(getOutput()).isEmpty();
    }

    @Test
    void disallowInvalidPathLength() {
        int returnCode = commandLine.execute(C2S_CMD, "--whole-program", "--max-path-length", "-2");
        assertThat(returnCode).isNotEqualTo(0);
        assertThat(getOutput()).isEmpty();
        assertThat(getErrorOutput()).contains("path length");
    }

    @Test
    void processEmptyProgram() {
        commandLine.execute(C2S_CMD, INPUT_FLAG, "src/test/fixtures/emptyProject.json");
        assertEmptyStdOut();
    }

    @Test
    void processProgramWithMultipleSprites() {
        commandLine.execute(C2S_CMD, INPUT_FLAG, "src/test/fixtures/multipleSprites.json");
        assertEmptyStdErr();

        final List<String> outputLines = getOutput().lines().toList();
        assertThat(outputLines).hasSize(3);
        assertThat(outputLines).containsExactly(
            "cat 39,29|57|41|8|25|153|27,hi|! 39,29|57|41|8|25|156,show hi|!,27|153|25|156,show",
            "abby green|flag,67|8|25|153|27,hello|!",
            "stage green|flag,67|8|25|26|29,10"
        );
    }

    @Test
    void processProgramWithMultipleSpritesToFile(@TempDir Path tempDir) throws IOException {
        commandLine.execute(
            C2S_CMD, INPUT_FLAG, "src/test/fixtures/multipleSprites.json", "-o", tempDir.toString()
        );
        assertEmptyStdErr();

        final List<String> outputLines = Files.readAllLines(tempDir.resolve("multipleSprites.txt"));
        assertThat(outputLines).hasSize(3);
        assertThat(outputLines).containsExactly(
            "cat 39,29|57|41|8|25|153|27,hi|! 39,29|57|41|8|25|156,show hi|!,27|153|25|156,show",
            "abby green|flag,67|8|25|153|27,hello|!",
            "stage green|flag,67|8|25|26|29,10"
        );
    }

    @Test
    void processProgramWithMultipleSpritesWholeProgram() {
        commandLine.execute(C2S_CMD, INPUT_FLAG, "src/test/fixtures/multipleSprites.json", "--whole-program");
        assertEmptyStdErr();

        final String output = getOutput();
        final List<String> outputLines = output.lines().toList();
        assertThat(outputLines).hasSize(1);

        assertThat(output).startsWith("program ");

        final Stream<String> expectedPaths = Stream.of(
            "39,29|57|41|8|25|153|27,hi|!",
            "39,29|57|41|8|25|156,show",
            "hi|!,27|153|25|156,show",
            "green|flag,67|8|25|153|27,hello|!"
        );
        assertAll(expectedPaths.map(path -> () -> assertThat(output).contains(path)));
    }

    @Test
    void processProgramWithMultipleSpritesPerScript() {
        commandLine.execute(C2S_CMD, INPUT_FLAG, "src/test/fixtures/multipleSprites.json", "--scripts");
        assertEmptyStdErr();
        assertStdOutContains("scriptId_-481429174");
    }

    @Test
    void processProgramWithMultipleSpritesPerScriptToFile(@TempDir File tempDir) throws IOException {
        commandLine.execute(
            C2S_CMD, INPUT_FLAG, "src/test/fixtures/multipleSprites.json", "--scripts", "-o", tempDir.toString()
        );
        assertEmptyStdErr();
        assertEmptyStdOut();

        final String output = Files.readString(tempDir.toPath().resolve("multipleSprites.script.txt"));
        assertThat(output)
            .contains(
                "scriptId_-481429174 39,29|57|41|8|25|153|27,hi|! 39,29|57|41|8|25|156,show hi|!,27|153|25|156,show"
            );
        assertThat(output).contains("scriptId_1341144038 green|flag,67|8|25|153|27,hello|!");
    }
}
