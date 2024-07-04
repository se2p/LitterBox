package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class IfThenStatementExecutionTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfThenStatementExecution(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfThenStatementExecution(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyIfThenStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfThenStatementExecution(), "src/test/fixtures/questions/oneVariableInIfStatement.json");
    }

    @Test
    public void testScriptWithOnlyIfElseStmt() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfThenStatementExecution(), "src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
    }

    @Test
    public void testScriptWithTwoIfThenStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new IfThenStatementExecution(), "src/test/fixtures/questions/twoIfThenStmtsInScript.json");
    }

    @Test
    public void testScriptWithNestedIfThenStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfThenStatementExecution(), "src/test/fixtures/questions/nestedIfThenStmts.json");
    }

    @Test
    public void testTwoScriptsWithIfThenStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new IfThenStatementExecution(), "src/test/fixtures/questions/twoScriptsWithBothIfStmts.json");
    }

    @Test
    public void testEmptyControlBody() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfThenStatementExecution(), "src/test/fixtures/questions/emptyIfThenBody.json");
    }

    @Test
    public void testHint() throws ParsingException, IOException {
        Program prog = getAST("src/test/fixtures/questions/twoScriptsWithBothIfStmts.json");
        Set<Issue> issues = runFinder(prog, new IfThenStatementExecution(), false);

        for (Issue issue : issues) {
            String hint = issue.getHint();
            if (hint.contains("FALSE"))
                assertThat(hint).containsMatch("\\[a-y]no\\[/a-y]");
            else
                assertThat(hint).containsMatch("\\[a-y]yes\\[/a-y]");
        }
    }
}
