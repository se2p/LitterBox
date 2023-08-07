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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2;

import de.uni_passau.fim.se2.litterbox.CliTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class Code2seqIntegrationTest extends CliTest {
    @Test
    void disallowBothWholeProgramAndPerSprite() {
        int returnCode = commandLine.execute("code2seq", "--whole-program", "--scripts");
        assertThat(returnCode).isNotEqualTo(0);
        assertThat(getOutput()).isEmpty();
    }

    @Test
    void processEmptyProgram() {
        commandLine.execute("code2seq", "-p", "src/test/fixtures/emptyProject.json");
        assertEmptyStdErr();
        assertEmptyStdOut();
    }

    @Test
    void processProgramWithMultipleSprites() {
        commandLine.execute("code2seq", "-p", "src/test/fixtures/multipleSprites.json", "--include-stage");
        assertEmptyStdErr();

        final List<String> outputLines = getOutput().lines().toList();
        assertThat(outputLines).hasSize(3);
        assertThat(outputLines).containsExactly(
                "cat 39,29|57|41|8|25|153|27,39 39,29|57|41|8|25|156,39 hi|!,27|153|25|156,hi|!",
                "abby green|flag,67|8|25|153|27,green|flag",
                "stage green|flag,67|8|25|26|29,green|flag"
        );
    }

    @Test
    void processProgramWithMultipleSpritesWholeProgram() {
        commandLine.execute("code2seq", "-p", "src/test/fixtures/multipleSprites.json", "--whole-program");
        assertEmptyStdErr();

        final String output = getOutput();
        final List<String> outputLines = output.lines().toList();
        assertThat(outputLines).hasSize(1);

        assertThat(output).startsWith("program ");

        final Stream<String> expectedPaths = Stream.of(
                "39,29|57|41|8|25|153|27,39 ",
                "39,29|57|41|8|25|156,39 ",
                "hi|!,27|153|25|156,hi|! ",
                "green|flag,67|8|25|153|27,green|flag"
        );
        assertAll(expectedPaths.map(path -> () -> assertThat(output).contains(path)));
    }

    @Test
    void processProgramWithMultipleSpritesPerScript() {
        commandLine.execute("code2seq", "-p", "src/test/fixtures/multipleSprites.json", "--scripts");
        assertEmptyStdErr();
        assertStdOutContains("scriptId_-481429174");
    }

    @Test
    void processProgramWithMultipleSpritesPerScriptToFile(@TempDir File tempDir) throws IOException {
        commandLine.execute(
                "code2seq", "-p", "src/test/fixtures/multipleSprites.json", "--scripts", "-o", tempDir.toString()
        );
        assertEmptyStdErr();
        assertEmptyStdOut();

        final String output = Files.readString(tempDir.toPath().resolve("multipleSprites_scriptId_-481429174"));
        assertThat(output).isEqualTo("39,29|57|41|8|25|153|27,39 39,29|57|41|8|25|156,39 hi|!,27|153|25|156,hi|!");
    }
}
