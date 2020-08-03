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
