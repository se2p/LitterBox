package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.UltraSonicOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UltraSonicOutOfBoundsTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new UltraSonicOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUltraSonicOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(4, new UltraSonicOutOfBounds(), "./src/test/fixtures/mblock/test/ultraSonicOutOfBounds.json");
    }
}
