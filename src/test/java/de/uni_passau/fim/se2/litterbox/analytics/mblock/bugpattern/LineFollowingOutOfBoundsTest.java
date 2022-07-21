package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.LineFollowingOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LineFollowingOutOfBoundsTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new LineFollowingOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testLineFollowingOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(1, new LineFollowingOutOfBounds(), "./src/test/fixtures/mblock/test/lineFollowingOutOfBounds.json");
    }
}
