package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ScriptExecutionOrderSameActorTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptExecutionOrderSameActor(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneScript() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptExecutionOrderSameActor(), "./src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testTwoScriptsDifferentEvents() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptExecutionOrderSameActor(), "./src/test/fixtures/questions/twoScriptsWithBothIfStmts.json");
    }

    @Test
    public void testTwoScriptsSameEvent() throws IOException, ParsingException {
        assertThatFinderReports(1, new ScriptExecutionOrderSameActor(), "./src/test/fixtures/questions/twoScriptsWithConditionalLoops.json");
    }

    @Test
    public void testTwoScriptsSameEventIn2Sprites() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptExecutionOrderSameActor(), "./src/test/fixtures/questions/scriptsWithSameEventIn2Sprites.json");
    }

    @Test
    public void testOneIssuePerActor() throws IOException, ParsingException {
        assertThatFinderReports(1, new ScriptExecutionOrderSameActor(), "./src/test/fixtures/questions/eventsTriggeredByStatements.json");
        assertThatFinderReports(2, new ScriptExecutionOrderSameActor(), "./src/test/fixtures/questions/sameEventsInDifferentScripts.json");
    }
}
