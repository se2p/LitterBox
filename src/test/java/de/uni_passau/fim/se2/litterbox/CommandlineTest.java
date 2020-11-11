/*
 * Copyright (C) 2020 LitterBox contributors
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;

public class CommandlineTest {

    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private final ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream mockErr = new ByteArrayOutputStream();

    @Test
    public void testInvalidOptionPrintsAnError() {
        Main.parseCommandLine(new String[]{"--optionthatdefinitelydoesntexist"});
        assertThat(mockErr.toString()).isNotEmpty();
    }

    @Test
    public void testPrintHelp() {
        Main.parseCommandLine(new String[]{"--output", "foobar"});
        assertThat(mockOut.toString()).contains("usage: LitterBox");
    }

    @Test
    public void testLeilaWithInvalidDownloadOption(@TempDir File tempFile) {
        String inputPath = tempFile.getAbsolutePath();
        Main.parseCommandLine(new String[] {"-leila", "--path", inputPath, "-o", "barfoo", "--projectid", "I am not a number"});
        Path path = Paths.get(inputPath, "I am not a number" + ".json");
        File file = new File(path.toString());
        assertThat(file.exists()).isFalse();
    }

    @Test
    public void testLeilaWithoutOutput() {
        Main.parseCommandLine(new String[] {"-leila"});
        assertThat(mockErr.toString()).contains("Invalid option: Output path option 'output' required");
    }

    @Test
    public void testLeilaWithoutPath() {
        Main.parseCommandLine(new String[] {"-leila", "--output", "foobar"});
        assertThat(mockErr.toString()).contains("Input path option 'path' required");
    }

    @Test
    public void testLeilaValidOptions(@TempDir File tempFile) throws Exception {
        File file = new File("./src/test/fixtures/emptyProject.json");
        String path = file.getAbsolutePath();
        String outFile = tempFile.getAbsolutePath();
        Main.parseCommandLine(new String[] {"-leila", "--path", path, "-o", outFile});
        String output = Files.readString(Paths.get(outFile, "emptyProject.sc"));
        assertThat(output.contains("program emptyProject"));
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

        System.setOut(new PrintStream(mockOut));
        System.setErr(new PrintStream(mockErr));
    }
}
