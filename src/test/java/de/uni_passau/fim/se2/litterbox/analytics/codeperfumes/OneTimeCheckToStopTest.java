package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class OneTimeCheckToStopTest implements JsonTest {

    @Test
    public void testEmpty() throws IOException, ParsingException {
        assertThatFinderReports(0, new OneTimeCheckToStop(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testOneTimeCheck() throws IOException, ParsingException {
        assertThatFinderReports(1, new OneTimeCheckToStop(), "./src/test/fixtures/goodPractice/oneTimeCheckStop.json");
    }
}
