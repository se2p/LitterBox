package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UndefinedBlockCountTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatMetricReports(0, new UndefinedBlockCount<>(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUndefinedBlockCountNested() throws IOException, ParsingException {
        assertThatMetricReports(3, new UndefinedBlockCount<>(), "./src/test/fixtures/metrics/undefinedBlocks.json");
    }

    @Test
    public void testUndefinedBlockCountWithEvent() throws IOException, ParsingException {
        assertThatMetricReports(4, new UndefinedBlockCount<>(), "./src/test/fixtures/metrics/undefinedBlocksEvent.json");
    }
}

