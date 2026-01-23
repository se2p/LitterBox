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
package de.uni_passau.fim.se2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Isolated;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;

@Isolated
public abstract class CliTest<T> {

    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private final ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream mockErr = new ByteArrayOutputStream();

    protected CommandLine commandLine;
    protected T mainClass;

    protected CliTest(T mainClass) {
        this.mainClass = mainClass;
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

        commandLine = new CommandLine(mainClass);
    }

    protected void assertStdOutContains(final String expected) {
        assertThat(getOutput()).contains(expected);
    }

    protected void assertStdErrContains(String expected) {
        assertThat(getErrorOutput()).contains(expected);
    }

    protected void assertEmptyStdErr() {
        assertThat(getErrorOutput()).isEmpty();
    }

    protected void assertEmptyStdOut() {
        assertThat(getOutput()).isEmpty();
    }

    protected String getOutput() {
        return mockOut.toString(StandardCharsets.UTF_8);
    }

    protected String getErrorOutput() {
        return mockErr.toString(StandardCharsets.UTF_8);
    }
}
