package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.smells.UnnecessaryTimeRobot;
import de.uni_passau.fim.se2.litterbox.analytics.smells.UnnecessaryTime;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UnnecessaryTimeRobotTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new UnnecessaryTimeRobot(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUnnecessaryTimeRobot() throws ParsingException, IOException {
        assertThatFinderReports(16, new UnnecessaryTimeRobot(), "./src/test/fixtures/mblock/test/unnecessaryTimeRobot.json");
    }
}
