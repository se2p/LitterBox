package de.uni_passau.fim.se2.litterbox.analytics.metrics;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LiteralCountTest implements JsonTest {

    @Test
    public void testAllMotion() throws IOException, ParsingException {
        assertThatMetricReports(14, new LiteralCount<>(), "./src/test/fixtures/metrics/allMotionBlocks.json");
    }

    @Test
    public void testAllLooks() throws IOException, ParsingException {
        assertThatMetricReports(11, new LiteralCount<>(), "./src/test/fixtures/metrics/allLooksBlocks.json");
    }

    @Test
    public void testAllSound() throws IOException, ParsingException {
        assertThatMetricReports(4, new LiteralCount<>(), "./src/test/fixtures/metrics/allSoundsBlocks.json");
    }

    @Test
    public void testAverageMotion() throws IOException, ParsingException {
        assertThatMetricReports(0.6666666666666666, new LiteralCount<>(true), "./src/test/fixtures/metrics/allMotionBlocks.json");
    }
}
