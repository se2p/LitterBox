package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RealCodeyMetricTest implements JsonTest {

    @Test
    public void testMetric() throws ParsingException, IOException {
        assertThatMetricReports(0, new RealCodeyMetric(), "./src/test/fixtures/mblock/test/codeyNotProgrammedMBotProgrammed.json");
        assertThatMetricReports(1, new RealMCoreMetric(), "./src/test/fixtures/mblock/test/codeyNotProgrammedMBotProgrammed.json");
    }
}
