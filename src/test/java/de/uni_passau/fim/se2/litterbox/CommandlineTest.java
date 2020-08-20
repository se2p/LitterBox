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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.google.common.truth.Truth.assertThat;

public class CommandlineTest {

    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
    private ByteArrayOutputStream mockErr = new ByteArrayOutputStream();

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
