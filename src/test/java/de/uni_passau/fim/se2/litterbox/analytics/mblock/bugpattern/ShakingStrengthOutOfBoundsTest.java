package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.ShakingStrengthOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ShakingStrengthOutOfBoundsTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new ShakingStrengthOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testShakingStrengthOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(6, new ShakingStrengthOutOfBounds(), "./src/test/fixtures/mblock/test/shakingStrengthOutOfBounds.json");
    }
}
