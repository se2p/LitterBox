package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LEDOffScriptTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new LEDOffScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void motorScreenCodey() throws ParsingException, IOException {
        assertThatFinderReports(0, new LEDOffScript(), "./src/test/fixtures/mblock/test/turnOffResourcesMotorScreen.json");
    }

    @Test
    public void LEDCodey() throws ParsingException, IOException {
        assertThatFinderReports(1, new LEDOffScript(), "./src/test/fixtures/mblock/test/LEDOffScript.json");
    }

    @Test
    public void LEDCodeyWorkaround() throws ParsingException, IOException {
        assertThatFinderReports(1, new LEDOffScript(), "./src/test/fixtures/mblock/test/LEDOffWorkaround.json");
    }
}
