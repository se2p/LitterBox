package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RollAngleInBoundsTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new RollAngleInBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testAllBoundaries() throws ParsingException, IOException {
        assertThatFinderReports(4, new RollAngleInBounds(), "./src/test/fixtures/mblock/test/rollAngleInBounds.json");
    }

    @Test
    public void test2In2Out() throws ParsingException, IOException {
        assertThatFinderReports(2, new RollAngleInBounds(), "./src/test/fixtures/mblock/test/rollAngle2in2out.json");
    }
}
