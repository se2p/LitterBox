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
package de.uni_passau.fim.se2.litterbox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;

public class CommandlineTest {

    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private final ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream mockErr = new ByteArrayOutputStream();

    private CommandLine commandLine;

    @Test
    public void testInvalidOptionPrintsAnError() {
        commandLine.execute("--optionthatdefinitelydoesntexist");
        assertThat(getErrorOutput()).isNotEmpty();
    }

    @Test
    public void testPrintHelp() {
        commandLine.execute("--output", "foobar");
        assertThatStdErrContains("Usage: LitterBox");
    }

    @Test
    public void testLeilaWithInvalidDownloadOption(@TempDir File tempFile) {
        String inputPath = tempFile.getAbsolutePath();
        commandLine.execute("leila", "--path", inputPath, "-o", "barfoo", "--projectid", "I am not a number");
        Path path = Paths.get(inputPath, "I am not a number" + ".json");
        File file = new File(path.toString());
        assertThat(file.exists()).isFalse();
    }

    @Test
    public void testLeilaWithoutOutput() {
        commandLine.execute("leila", "--path", "foobar");
        assertThatStdErrContains("Output path option '--output' required");
    }

    @Test
    public void testLeilaWithoutPath() {
        commandLine.execute("leila", "--output", "foobar");
        assertThatStdErrContains("Input path option '--path' required");
    }

    @Test
    public void testLeilaValidOptions(@TempDir File tempFile) throws Exception {
        File file = new File("./src/test/fixtures/emptyProject.json");
        String path = file.getAbsolutePath();
        String outFile = tempFile.getAbsolutePath();
        commandLine.execute("leila", "--path", path, "-o", outFile);
        String output = Files.readString(Paths.get(outFile, "emptyProject.sc"));
        assertThat(output).contains("program emptyProject");
    }

    private void assertThatStdErrContains(String expected) {
        assertThat(getErrorOutput()).contains(expected);
    }

    private String getErrorOutput() {
        return mockErr.toString(StandardCharsets.UTF_8);
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(out);
        System.setErr(err);
    }

    @BeforeEach
    public void replaceStreams() {
        out = System.out;
        err = System.err;
        mockErr.reset();
        mockOut.reset();

        System.setOut(new PrintStream(mockOut, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(mockErr, true, StandardCharsets.UTF_8));

        commandLine = new CommandLine(new Main());
    }
}
