package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RobotMetricsTest implements JsonTest {

    @Test
    public void testRobotCodeMetric() throws ParsingException, IOException {
        assertThatMetricReports(1, new RobotCodeMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }

    @Test
    public void testCodeyCounterMetric() throws ParsingException, IOException {
        assertThatMetricReports(8, new CodeyCounterMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }

    @Test
    public void testMCoreCounterMetric() throws ParsingException, IOException {
        assertThatMetricReports(1, new MCoreCounterMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }

    @Test
    public void testAurigaCounterMetric() throws ParsingException, IOException {
        assertThatMetricReports(1, new AurigaCounterMetric(), "./src/test/fixtures/mblock/failed/421199.json");
    }

    @Test
    public void testRobotCodeMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new RobotCodeMetric(), "./src/test/fixtures/allBlocks.json");
    }

    @Test
    public void testCodeyCounterMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new CodeyCounterMetric(), "./src/test/fixtures/allBlocks.json");
    }

    @Test
    public void testMCoreCounterMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new MCoreCounterMetric(), "./src/test/fixtures/allBlocks.json");
    }

    @Test
    public void testAurigaCounterMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new AurigaCounterMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }

    @Test
    public void testMegapiCounterMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new MegapiCounterMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }
}
