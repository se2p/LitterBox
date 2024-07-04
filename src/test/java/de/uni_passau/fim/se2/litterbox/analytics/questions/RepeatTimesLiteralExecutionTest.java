package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RepeatTimesLiteralExecutionTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoLoops() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyForeverLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/scriptWithOnlyForeverLoop.json");
    }

    @Test
    public void testScriptWithOnlyTimesLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/scriptWithOnlyTimesLoop.json");
    }

    @Test
    public void testRepeatTimesStmtsWithDifferentOvals() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesStmtsWithDifferentOvals.json");
    }

    @Test
    public void testScriptWithOnlyUntilLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/scriptWithOnlyUntilLoop.json");
    }

    @Test
    public void testRepeatTimesWithStop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesWithStop.json");
    }

    @Test
    public void testRepeatTimesWithStopInIfElse() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesWithStopInIfThen.json");
    }

    @Test
    public void testRepeatTimesWithStopInNestedLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesWithStopInNestedLoop.json");
    }

    @Test
    public void testScriptWithTwoLoopsAndOtherElements() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/scriptWithTwoLoopsAndOtherElements.json");
    }

    @Test
    public void testNestedRepeatTimesStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/nestedRepeatTimesStmts.json");
    }

    @Test
    public void testRepeatTimesStmtInLoopStmt() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesInLoop.json");
    }

    @Test
    public void testTwoScriptsWithConditionalLoops() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/twoScriptsWithConditionalLoops.json");
    }

    @Test
    public void testTwoRepeatTimesStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/twoRepeatTimesStmts.json");
    }
}
