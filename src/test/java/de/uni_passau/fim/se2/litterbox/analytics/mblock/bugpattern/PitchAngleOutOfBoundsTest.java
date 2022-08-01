package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.PitchAngleOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PitchAngleOutOfBoundsTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new PitchAngleOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testPitchAngleOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(6, new PitchAngleOutOfBounds(), "./src/test/fixtures/mblock/test/pitchAngleOutOfBounds.json");
    }
}
