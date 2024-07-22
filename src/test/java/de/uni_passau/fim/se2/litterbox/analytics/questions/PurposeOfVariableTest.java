package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PurposeOfVariableTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new PurposeOfVariable(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneVariable() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfVariable(), "src/test/fixtures/questions/oneVariable.json");
    }

    @Test
    public void testOneVariableAppearsTwice() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfVariable(), "src/test/fixtures/questions/oneVariableAppearsInTwoSeparateScripts.json");
    }

    @Test
    public void testTwoVariable() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfVariable(), "src/test/fixtures/questions/twoVariables.json");
    }
}
