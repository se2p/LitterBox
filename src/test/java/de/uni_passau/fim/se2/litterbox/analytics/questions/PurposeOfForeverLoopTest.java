package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PurposeOfForeverLoopTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new PurposeOfForeverLoop(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfForeverLoop(), "src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testTwoLoops() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfForeverLoop(), "src/test/fixtures/questions/twoForeverStmts.json");
    }

    @Test
    public void testNestedLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfForeverLoop(), "src/test/fixtures/questions/nestedForeverStmt.json");
    }
}
