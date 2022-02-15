package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MissingLoopMousePositionTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingLoopMousePosition(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testMovesMouse() throws IOException, ParsingException {
        assertThatFinderReports(7, new MissingLoopMousePosition(), "./src/test/fixtures/bugpattern/missingLoopMousePosition.json");
    }
}
