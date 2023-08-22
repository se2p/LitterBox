package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ElementInLoopConditionTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new ElementInLoopCondition(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoLoops() throws IOException, ParsingException {
        assertThatFinderReports(0, new ElementInLoopCondition(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyForeverLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new ElementInLoopCondition(), "src/test/fixtures/questions/scriptWithOnlyForeverLoop.json");
    }

    @Test
    public void testScriptWithOnlyTimesLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new ElementInLoopCondition(), "src/test/fixtures/questions/scriptWithOnlyTimesLoop.json");
    }

    @Test
    public void testScriptWithOnlyUntilLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new ElementInLoopCondition(), "src/test/fixtures/questions/scriptWithOnlyUntilLoop.json");
    }

    @Test
    public void testScriptWithForeverLoopAndOtherElement() throws IOException, ParsingException {
        assertThatFinderReports(0, new ElementInLoopCondition(), "src/test/fixtures/questions/scriptWithForeverLoopAndOtherElement.json");
    }

    @Test
    public void testOneScriptWithForeverLoopAndOneWithOtherElement() throws IOException, ParsingException {
        assertThatFinderReports(0, new ElementInLoopCondition(), "src/test/fixtures/questions/oneScriptWithForeverLoopOneWithOtherElement.json");
    }

    @Test
    public void testScriptWithTwoConditionLoopsAndOtherElements() throws IOException, ParsingException {
        assertThatFinderReports(1, new ElementInLoopCondition(), "src/test/fixtures/questions/scriptWithTwoLoopsAndOtherElements.json");
    }

    @Test
    public void testTwoScriptsWithConditionalLoops() throws IOException, ParsingException {
        assertThatFinderReports(2, new ElementInLoopCondition(), "src/test/fixtures/questions/twoScriptsWithConditionalLoops.json");
    }
}
