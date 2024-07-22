package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PurposeOfLoopConditionTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new PurposeOfLoopCondition(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneUntilStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfLoopCondition(), "src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testTwoUntilStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfLoopCondition(), "src/test/fixtures/questions/twoRepeatUntilStmts.json");
    }

    @Test
    public void testNestedUntilStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfLoopCondition(), "src/test/fixtures/questions/nestedRepeatUntilStmts.json");
    }
}
