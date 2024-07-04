package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BlockControllingLoopTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new BlockControllingLoop(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoLoops() throws IOException, ParsingException {
        assertThatFinderReports(0, new BlockControllingLoop(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyForeverLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new BlockControllingLoop(), "src/test/fixtures/questions/scriptWithOnlyForeverLoop.json");
    }

    @Test
    public void testScriptWithOnlyTimesLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new BlockControllingLoop(), "src/test/fixtures/questions/scriptWithOnlyTimesLoop.json");
    }

    @Test
    public void testScriptWithOnlyUntilLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new BlockControllingLoop(), "src/test/fixtures/questions/scriptWithOnlyUntilLoop.json");
    }

    @Test
    public void testScriptWithTwoConditionLoops() throws IOException, ParsingException {
        assertThatFinderReports(1, new BlockControllingLoop(), "src/test/fixtures/questions/twoControlStmtsInScript.json");
    }

    @Test
    public void testAllControlBlocks() throws IOException, ParsingException {
        assertThatFinderReports(2, new BlockControllingLoop(), "src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testNestedControlBlock() throws IOException, ParsingException {
        assertThatFinderReports(1, new BlockControllingLoop(), "src/test/fixtures/questions/nestedControlBlock.json");
    }

    @Test
    public void testRepeatWithNumExprs() throws IOException, ParsingException {
        assertThatFinderReports(1, new BlockControllingLoop(), "src/test/fixtures/questions/repeatWithNumExprs.json");
    }

    @Test
    public void testRepeatTimesWithOnlyStop() throws IOException, ParsingException {
        assertThatFinderReports(0, new BlockControllingLoop(), "src/test/fixtures/questions/repeatTimesWithOnlyStop.json");
    }
}
