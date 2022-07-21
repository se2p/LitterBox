package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes.BatteryLevelInBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BatteryBlockCountTest implements JsonTest {

    @Test
    public void test2BatterBlocks() throws ParsingException, IOException {
        assertThatMetricReports(2, new BatteryBlockCount<>(), "./src/test/fixtures/mblock/test/battery2blocks.json");
        assertThatFinderReports(1, new BatteryLevelInBounds(), "./src/test/fixtures/mblock/test/battery2blocks.json");
    }
}
