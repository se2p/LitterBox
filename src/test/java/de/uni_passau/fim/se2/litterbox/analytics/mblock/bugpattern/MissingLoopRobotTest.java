package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MissingLoopRobotTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new MissingLoopRobotSensing(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testMissingLoopRobotSensing() throws ParsingException, IOException {
        assertThatFinderReports(10, new MissingLoopRobotSensing(), "./src/test/fixtures/mblock/test/missingLoopRobot.json");
    }
}
