package de.uni_passau.fim.se2.litterbox.analytics.mblock.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MotorPowerMinusTest implements JsonTest {

    @Test
    public void emptyProject() throws ParsingException, IOException {
        assertThatFinderReports(0, new MotorPowerMinus(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testMinus() throws ParsingException, IOException {
        assertThatFinderReports(3, new MotorPowerMinus(), "./src/test/fixtures/mblock/test/motorPowerMinus.json");
    }
}
