package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class IfElseStatementExecutionTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfElseStatementExecution(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfElseStatementExecution(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyIfThenStmt() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfElseStatementExecution(), "src/test/fixtures/questions/oneVariableInIfStatement.json");
    }

    @Test
    public void testScriptWithOnlyIfElseStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfElseStatementExecution(), "src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
    }

    @Test
    public void testScriptWithTwoIfElseStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new IfElseStatementExecution(), "src/test/fixtures/questions/twoIfElseStmtsInScript.json");
    }

    @Test
    public void testScriptWithNestedIfElseStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfElseStatementExecution(), "src/test/fixtures/questions/nestedIfElseStmts.json");
    }

    @Test
    public void testTwoScriptsWithIfElseStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new IfElseStatementExecution(), "src/test/fixtures/questions/twoScriptsWithBothIfStmts.json");
    }

    @Test
    public void testEmptyControlBody() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfElseStatementExecution(), "src/test/fixtures/questions/emptyIfElseBody.json");
    }

    @Test
    public void testHint() throws ParsingException, IOException {
        Program prog = getAST("src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
        Set<Issue> issues = runFinder(prog, new IfElseStatementExecution(), false);

        for (Issue issue : issues) {
            String hint = issue.getHint();
            if (hint.contains("TRUE"))
                assertThat(hint).containsMatch("\\[a-c]\\[scratchblocks]\nset \\[new variable v] to \\(0\\)");
            else
                assertThat(hint).containsMatch("\\[a-c]\\[scratchblocks]\nplay sound \\(Meow v\\) until done");
        }
    }
}
