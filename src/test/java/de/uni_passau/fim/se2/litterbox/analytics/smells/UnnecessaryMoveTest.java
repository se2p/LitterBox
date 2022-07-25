package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UnnecessaryMoveTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryMove(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUnnecessaryRotation() throws IOException, ParsingException {
        assertThatFinderReports(3, new UnnecessaryMove(), "./src/test/fixtures/smells/unnecessaryMove.json");
    }
}
