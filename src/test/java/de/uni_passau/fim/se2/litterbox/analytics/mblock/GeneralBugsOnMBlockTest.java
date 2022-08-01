package de.uni_passau.fim.se2.litterbox.analytics.mblock;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.smells.DeadCode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class GeneralBugsOnMBlockTest implements JsonTest {

    @Test
    public void testDeadCode() throws IOException, ParsingException {
        assertThatFinderReports(1, new DeadCode(), "./src/test/fixtures/mblock/dead_code_test.json");
    }
}
