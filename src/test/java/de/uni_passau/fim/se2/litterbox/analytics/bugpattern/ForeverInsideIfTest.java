package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ForeverInsideIfTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new ForeverInsideIf(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testForeverInLoop() throws IOException, ParsingException {
        assertThatFinderReports(2, new ForeverInsideIf(), "./src/test/fixtures/bugpattern/foreverInsideIf.json");
    }
}
