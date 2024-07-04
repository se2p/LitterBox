package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class VariableInScriptTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableInScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoVariables() throws IOException, ParsingException {
        assertThatFinderReports(0, new VariableInScript(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testOneVariable() throws IOException, ParsingException {
        assertThatFinderReports(1, new VariableInScript(), "src/test/fixtures/questions/oneVariableInIfStatement.json");
    }

    @Test
    public void testOneVariableAppearsInTwoSeparateScripts() throws IOException, ParsingException {
        assertThatFinderReports(2, new VariableInScript(), "src/test/fixtures/questions/oneVariableAppearsInTwoSeparateScripts.json");
    }

    @Test
    public void testOneVariableAppearsTwiceInScript() throws IOException, ParsingException {
        assertThatFinderReports(1, new VariableInScript(), "src/test/fixtures/questions/oneVariableAppearsTwiceInScript.json");

    }

    @Test
    public void testTwoVariables() throws IOException, ParsingException {
        assertThatFinderReports(1, new VariableInScript(), "src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
    }

    @Test
    public void testFourScriptsThreeWithVariables() throws IOException, ParsingException {
        assertThatFinderReports(3, new VariableInScript(), "src/test/fixtures/questions/fourScriptsThreeWithVariables.json");
    }

    @Test
    public void testHint() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
        Set<Issue> issues = runFinder(prog, new VariableInScript(), false);

        for (Issue issue : issues) {
            String hint = issue.getHint();
            assertThat(hint).contains("my variable");
            assertThat(hint).contains("new variable");
        }
    }
}
