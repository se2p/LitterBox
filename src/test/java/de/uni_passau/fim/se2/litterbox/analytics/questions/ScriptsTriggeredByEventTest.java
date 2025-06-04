package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class ScriptsTriggeredByEventTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptsTriggeredByEvent(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneScript() throws IOException, ParsingException {
        assertThatFinderReports(1, new ScriptsTriggeredByEvent(), "./src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testTwoScriptsWithSameEvent() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/oneScriptWithForeverLoopOneWithOtherElement.json");
        Set<Issue> issues = runFinder(prog, new ScriptsTriggeredByEvent(), false);
        assertThat(issues.size()).isEqualTo(1);

        for (Issue issue : issues) {
            assertThat(issue.getHint().getHintText()).containsMatch("\\[a-n]2\\[/a-n]");
        }
    }

    @Test
    public void testTwoScriptsWithDifferentEvents() throws IOException, ParsingException {
        assertThatFinderReports(2, new ScriptsTriggeredByEvent(), "./src/test/fixtures/questions/twoScriptsWithBothIfStmts.json");
    }

    @Test
    public void testMultipleEventsInDifferentActors() throws IOException, ParsingException {
        assertThatFinderReports(5, new ScriptsTriggeredByEvent(), "./src/test/fixtures/questions/eventsTriggeredByStatements.json");
    }

    @Test
    public void testScriptWithNoEvent() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptsTriggeredByEvent(), "./src/test/fixtures/questions/scriptWithNoEvent.json");
    }
}
