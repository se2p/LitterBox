package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class ScriptExecutionOrderDifferentActorsTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptExecutionOrderDifferentActors(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneScript() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptExecutionOrderDifferentActors(), "./src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testTwoScriptsSameEventSameActor() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptExecutionOrderDifferentActors(), "./src/test/fixtures/questions/twoScriptsWithConditionalLoops.json");
    }

    @Test
    public void testTwoScriptsDifferentEventIn2Sprites() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptExecutionOrderDifferentActors(), "./src/test/fixtures/questions/scriptsWithDifferentEventIn2Sprites.json");
    }

    @Test
    public void testSameEventsDifferentSprites() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/sameEventsInDifferentScripts.json");
        Set<Issue> issues = runFinder(prog, new ScriptExecutionOrderDifferentActors(), false);
        assertThat(issues.size()).isEqualTo(2);

        for (Issue issue : issues) {
            if (issue.getHint().contains("Stage")) {
                assertThat(issue.getHint()).contains("[solutions]The script belonging to Sprite1[/solutions]");
            }
            else {
                assertThat(issue.getHint()).contains("Suppose");
            }
        }
    }
}
