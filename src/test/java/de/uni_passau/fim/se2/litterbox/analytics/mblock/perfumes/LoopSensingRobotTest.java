package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LoopSensingRobotTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new LoopSensingRobot(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testLoop() throws ParsingException, IOException {
        assertThatFinderReports(3, new LoopSensingRobot(), "./src/test/fixtures/mblock/test/loopSensingRobot.json");
    }
}
