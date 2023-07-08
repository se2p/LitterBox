package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class VariableInScriptTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableInScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoVariables() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableInScript(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testOneVariable() throws IOException, ParsingException {
        assertThatFinderReports(1, new VariableInScript(), "src/test/fixtures/questions/oneVariable.json");
    }

    @Test
    public void testOneVariableAppearsInTwoSeparateScripts() throws IOException, ParsingException {
        assertThatFinderReports(2, new VariableInScript(), "src/test/fixtures/questions/oneVariableAppearsInTwoSeparateScripts.json");
    }

    @Test
    public void testOneVariableAppearsTwiceInScript() throws IOException, ParsingException {
        assertThatFinderReports(1, new VariableInScript(), "src/test/fixtures/questions/oneVariableAppearsTwiceInScript.json");

    }

    @Test
    public void testTwoVariables() throws IOException, ParsingException {
        assertThatFinderReports(1, new VariableInScript(), "src/test/fixtures/questions/twoVariables.json");
    }

    @Test
    public void testFourScriptsThreeWithVariabless() throws IOException, ParsingException {
        assertThatFinderReports(3, new VariableInScript(), "src/test/fixtures/questions/fourScriptsThreeWithVariables.json");
    }
}
