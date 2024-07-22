package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PurposeOfScriptTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new PurposeOfScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneScript() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfScript(), "./src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testTwoScripts() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfScript(), "./src/test/fixtures/questions/twoScriptsWithConditionalLoops.json");
    }
}
