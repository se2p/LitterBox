package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BooleanOperationCountTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatMetricReports(0, new BooleanOperationCount<>(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testBooleanOperationCountNested() throws IOException, ParsingException {
        assertThatMetricReports(3, new BooleanOperationCount<>(), "./src/test/fixtures/metrics/booleanOperationsNested.json");
    }

    @Test
    public void testBooleanOperationCount() throws IOException, ParsingException {
        assertThatMetricReports(3, new BooleanOperationCount<>(), "./src/test/fixtures/metrics/booleanOperations.json");
    }
}

