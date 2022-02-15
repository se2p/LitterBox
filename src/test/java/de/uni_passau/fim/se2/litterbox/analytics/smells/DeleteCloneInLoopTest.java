package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DeleteCloneInLoopTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new DeleteCloneInLoop(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testDeleteCloneInLoop() throws IOException, ParsingException {
        assertThatFinderReports(2, new DeleteCloneInLoop(), "./src/test/fixtures/smells/deleteCloneInLoop.json");
    }

    @Test
    public void testDeleteCloneInLoopIf() throws IOException, ParsingException {
        assertThatFinderReports(0, new DeleteCloneInLoop(), "./src/test/fixtures/smells/deleteCloneInLoopIf.json");
    }
}
