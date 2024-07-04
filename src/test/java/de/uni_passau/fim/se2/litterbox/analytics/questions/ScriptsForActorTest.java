package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class ScriptsForActorTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/emptyProject.json");
        Set<Issue> issues = runFinder(prog, new ScriptsForActor(), false);
        assertThat(issues.size()).isEqualTo(2);

        for (Issue issue : issues) {
            assertThat(issue.getHint()).containsMatch("\\[a-n]0\\[/a-n]");
        }
    }

    @Test
    public void testOneSpriteWithOneScript() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/onlyStageOneScript.json");
        Set<Issue> issues = runFinder(prog, new ScriptsForActor(), false);
        assertThat(issues.size()).isEqualTo(1);

        for (Issue issue : issues) {
            assertThat(issue.getHint()).containsMatch("\\[a-n]1\\[/a-n]");
        }
    }

    @Test
    public void testOneSpriteWithTwoScripts() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/onlyStageTwoScripts.json");
        Set<Issue> issues = runFinder(prog, new ScriptsForActor(), false);
        assertThat(issues.size()).isEqualTo(1);

        for (Issue issue : issues) {
            assertThat(issue.getHint()).containsMatch("\\[a-n]2\\[/a-n]");
        }
    }

    @Test
    public void testMultipleScriptsInDifferentActors() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/eventsTriggeredByStatements.json");
        Set<Issue> issues = runFinder(prog, new ScriptsForActor(), false);
        assertThat(issues.size()).isEqualTo(2);

        for (Issue issue : issues) {
            String hint = issue.getHint();
            if (hint.contains("Sprite1"))
                assertThat(hint).containsMatch("\\[a-n]6\\[/a-n]");
            else
                assertThat(hint).containsMatch("\\[a-n]2\\[/a-n]");
        }
    }
}
