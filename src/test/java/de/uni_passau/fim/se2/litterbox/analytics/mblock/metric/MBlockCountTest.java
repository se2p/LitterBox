package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MBlockCountTest implements JsonTest {

    @Test
    public void testMBlockCountMetric() throws ParsingException, IOException {
        assertThatMetricReports(7, new MBlockCount<>(), "./src/test/fixtures/mblock/mBlockCount.json");
    }

    @Test
    public void testMBlockCountFixedMetric() throws ParsingException, IOException {
        assertThatMetricReports(7, new MBlockCount<>(), "./src/test/fixtures/mblock/mBlockCountFixed.json");
    }
}
