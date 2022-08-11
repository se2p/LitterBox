package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class VariablesBlockCountOnlyDirectVariableTest implements JsonTest {
    @Test
    public void testAll() throws IOException, ParsingException {
        assertThatMetricReports(1, new VariablesBlockCountOnlyDirectVariable<>(), "./src/test/fixtures/metrics/variableCount.json");
    }
}
