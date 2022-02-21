package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UnnecessaryStopScriptTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryStopScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoStopScript() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryStopScript(), "./src/test/fixtures/smells/unnecessaryWait.json");
    }

    @Test
    public void testStopScript() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnnecessaryStopScript(), "./src/test/fixtures/smells/stopscript.json");
    }

    @Test
    public void testStopOtherScript() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryStopScript(), "./src/test/fixtures/smells/stopotherscript.json");
    }

    @Test
    public void testStopAllScripts() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryStopScript(), "./src/test/fixtures/smells/stopallscripts.json");
    }
}
