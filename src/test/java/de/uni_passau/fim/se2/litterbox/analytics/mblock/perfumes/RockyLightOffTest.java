package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RockyLightOffTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new RockyLightOffScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void motorScreenCodey() throws ParsingException, IOException {
        assertThatFinderReports(0, new RockyLightOffScript(), "./src/test/fixtures/mblock/test/turnOffResourcesMotorScreen.json");
    }

    @Test
    public void RockyLight() throws ParsingException, IOException {
        assertThatFinderReports(1, new RockyLightOffScript(), "./src/test/fixtures/mblock/test/rockyLightOff.json");
    }
}
