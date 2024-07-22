package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PurposeOfRepeatUntilLoopTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new PurposeOfRepeatUntilLoop(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfRepeatUntilLoop(), "src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testTwoLoops() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfRepeatUntilLoop(), "src/test/fixtures/questions/twoRepeatUntilStmts.json");
    }

    @Test
    public void testNestedLoop() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfRepeatUntilLoop(), "src/test/fixtures/questions/nestedRepeatUntilStmts.json");
    }
}
