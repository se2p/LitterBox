package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class IfBlockConditionTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfBlockCondition(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfBlockCondition(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyIfThenStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/oneVariableInIfStatement.json");
    }

    @Test
    public void testScriptWithOnlyIfElseStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
    }

    @Test
    public void testScriptWithTwoIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/twoIfThenStmtsInScript.json");
    }

    @Test
    public void testScriptWithNestedIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/nestedIfThenStmts.json");
    }

    @Test
    public void testAllControlBlocks() throws IOException, ParsingException {
        assertThatFinderReports(2, new IfBlockCondition(), "src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testNestedControlBlock() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/nestedControlBlock.json");
    }

    @Test
    public void testEmptyControlBody() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfBlockCondition(), "src/test/fixtures/questions/emptyIfThenBody.json");
    }
}
