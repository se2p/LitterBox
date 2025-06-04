package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;

class StatementTriggersEventTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new StatementTriggersEvent(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoRelevantElements() throws IOException, ParsingException {
        assertThatFinderReports(0, new StatementTriggersEvent(), "./src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testBackdropSwitchEventWithNoStmt() throws IOException, ParsingException {
        assertThatFinderReports(0, new StatementTriggersEvent(), "src/test/fixtures/questions/fourScriptsThreeWithVariables.json");
    }

    @Test
    public void testBroadcastStmtWithNoEvent() throws IOException, ParsingException {
        assertThatFinderReports(0, new StatementTriggersEvent(), "./src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testBothEventsAndStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new StatementTriggersEvent(), "./src/test/fixtures/questions/eventsTriggeredByStatements.json");
    }

    @Test
    public void testHintLimitedChoices() throws ParsingException, IOException {
        Program prog = getAST("src/test/fixtures/questions/eventsTriggeredByStatements.json");
        Set<Issue> issues = runFinder(prog, new StatementTriggersEvent(), false);

        for (Issue issue : issues) {
            String hint = issue.getHint().getHintText();
            String pattern = "\\[choices](\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\|){3}\\[sbi](?:(?!\\[sbi]).)*\\[/sbi]\\[/choices]";
            assertThat(hint).containsMatch(Pattern.compile(pattern, Pattern.DOTALL));
        }
    }
}
