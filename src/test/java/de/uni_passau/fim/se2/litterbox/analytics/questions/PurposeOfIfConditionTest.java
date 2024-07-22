package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PurposeOfIfConditionTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new PurposeOfIfCondition(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneIfThenStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new PurposeOfIfCondition(), "src/test/fixtures/questions/ifStmtAndOtherStmt.json");
    }

    @Test
    public void testTwoIfElseStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfIfCondition(), "src/test/fixtures/questions/twoIfElseStmtsInScript.json");
    }

    @Test
    public void testTwoIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfIfCondition(), "src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testNestedIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new PurposeOfIfCondition(), "src/test/fixtures/questions/nestedIfElseStmts.json");
    }
}
