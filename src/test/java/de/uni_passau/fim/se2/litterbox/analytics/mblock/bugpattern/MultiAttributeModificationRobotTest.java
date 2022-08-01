package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.smells.MultiAttributeModificationRobot;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MultiAttributeModificationRobotTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new MultiAttributeModificationRobot(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testMultiAttributeModificationRobot() throws ParsingException, IOException {
        assertThatFinderReports(11, new MultiAttributeModificationRobot(), "./src/test/fixtures/mblock/test/multiAttributeRobot.json");
    }
}
