package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.smells.MultiAttributeModificationRobot;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class InterruptedLoopSensingRobotTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new InterruptedLoopSensingRobot(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testInterruptedLoopSensingRobot() throws ParsingException, IOException {
        assertThatFinderReports(3, new InterruptedLoopSensingRobot(), "./src/test/fixtures/mblock/test/interruptedLoopRobot.json");
    }
}
