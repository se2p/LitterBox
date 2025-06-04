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
package de.uni_passau.fim.se2.litterbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

public class CommandlineTest extends CliTest {

    @Test
    public void testInvalidOptionPrintsAnError() {
        commandLine.execute("--optionthatdefinitelydoesntexist");
        assertThat(getErrorOutput()).isNotEmpty();
    }

    @Test
    public void testPrintHelp() {
        commandLine.execute("--output", "foobar");
        assertStdErrContains("Usage: LitterBox");
    }

    @Test
    public void testLeilaWithInvalidDownloadOption(@TempDir File tempFile) {
        Path inputPath = tempFile.toPath().toAbsolutePath();
        commandLine.execute(
                "leila", "--path", inputPath.toString(), "-o", "barfoo", "--projectid", "I am not a number"
        );
        Path path = inputPath.resolve("I am not a number" + ".json");
        File file = new File(path.toString());
        assertThat(file.exists()).isFalse();
    }

    @Test
    public void testLeilaWithoutOutput() {
        commandLine.execute("leila", "--path", "foobar");
        assertStdErrContains("Output path option '--output' required");
    }

    @Test
    public void testLeilaWithoutPath() {
        commandLine.execute("leila", "--output", "foobar");
        assertStdErrContains("Input path option '--path' required");
    }

    @Test
    public void testLeilaValidOptions(@TempDir File tempFile) throws Exception {
        File file = new File("./src/test/fixtures/emptyProject.json");
        Path path = file.toPath().toAbsolutePath();
        Path outFile = tempFile.toPath().toAbsolutePath();
        commandLine.execute("leila", "--path", path.toString(), "-o", outFile.toString());
        String output = Files.readString(outFile.resolve("emptyProject.sc"));
        assertThat(output).contains("program emptyProject");
    }
}
