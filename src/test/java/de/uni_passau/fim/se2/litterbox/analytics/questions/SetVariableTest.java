package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class SetVariableTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new SetVariable(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testSetVariableToNumber() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/setVariableToNumberLiteral.json");
        Set<Issue> issues = runFinder(prog, new SetVariable(), false);
        assertThat(issues.size()).isEqualTo(1);

        for (Issue issue : issues) {
            assertThat(issue.getHint()).containsMatch("\\[a-s]0\\[/a-s]");
        }
    }

    @Test
    public void testSetVariableToString() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/setVariableToStringLiteral.json");
        Set<Issue> issues = runFinder(prog, new SetVariable(), false);
        assertThat(issues.size()).isEqualTo(1);

        for (Issue issue : issues) {
            assertThat(issue.getHint()).containsMatch("\\[a-s]Hello\\[/a-s]");
        }
    }

    @Test
    public void testSetVariableToNonLiterals() throws IOException, ParsingException {
        assertThatFinderReports(0, new SetVariable(), "./src/test/fixtures/questions/setVariableToNonLiterals.json");
    }

    @Test
    public void testSetVariableTwice() throws IOException, ParsingException {
        assertThatFinderReports(2, new SetVariable(), "./src/test/fixtures/questions/setVariableTwice.json");
    }

}
