package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MotorPowerOutOfBoundsTest implements JsonTest {

    @Test
    public void emptyProject() throws ParsingException, IOException {
        assertThatFinderReports(0, new MotorPowerOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testMotorPowerBounds() throws ParsingException, IOException {
        assertThatFinderReports(4, new MotorPowerOutOfBounds(), "./src/test/fixtures/mblock/test/MotorPowerOutOfBounds.json");
    }

    @Test
    public void testMotorPowerBounds_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new MotorPowerOutOfBounds(), "./src/test/fixtures/mblock/test/MotorPowerOutOfBounds_Sol.json");
    }
}
