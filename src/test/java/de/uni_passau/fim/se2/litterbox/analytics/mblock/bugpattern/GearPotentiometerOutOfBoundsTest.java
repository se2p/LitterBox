package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.GearPotentiometerOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class GearPotentiometerOutOfBoundsTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new GearPotentiometerOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testGearPotentiometerOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(6, new GearPotentiometerOutOfBounds(), "./src/test/fixtures/mblock/test/gearOutOfBounds.json");
    }

    @Test
    public void testGearPotentiometerDoubleOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(1, new GearPotentiometerOutOfBounds(), "./src/test/fixtures/mblock/test/GearPotentiometerDouble.json");
    }
}
