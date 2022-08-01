package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ParallelisationRobotTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new ParallelisationRobot(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testParallel() throws ParsingException, IOException {
        assertThatFinderReports(6, new ParallelisationRobot(), "./src/test/fixtures/mblock/test/parallelisation_robot.json");
    }
}
