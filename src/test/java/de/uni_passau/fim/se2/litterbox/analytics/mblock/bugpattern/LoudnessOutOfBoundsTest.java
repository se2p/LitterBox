package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.LoudnessOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LoudnessOutOfBoundsTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new LoudnessOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void loudnessOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(6, new LoudnessOutOfBounds(), "./src/test/fixtures/mblock/test/loudnessOutOfBounds.json");
    }
}
