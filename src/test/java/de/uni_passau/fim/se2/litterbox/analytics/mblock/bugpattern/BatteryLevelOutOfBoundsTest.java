package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.BatteryLevelOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BatteryLevelOutOfBoundsTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new BatteryLevelOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testBatteryLevelOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(6, new BatteryLevelOutOfBounds(), "./src/test/fixtures/mblock/test/batteryLevelOutOfBounds.json");
    }
}
