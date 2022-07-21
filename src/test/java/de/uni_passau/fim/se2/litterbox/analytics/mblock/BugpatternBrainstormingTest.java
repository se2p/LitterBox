package de.uni_passau.fim.se2.litterbox.analytics.mblock;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BugpatternBrainstormingTest implements JsonTest {

    @Test
    public void testASMotorStopScriptMissingBug() throws ParsingException, IOException {
        assertThatFinderReports(2, new MotorStopScriptMissing(), "./src/test/fixtures/mblock/brainstorming/AS_bug.json");
    }

    @Test
    public void testASMotorStopScriptMissingSol() throws ParsingException, IOException {
        assertThatFinderReports(0, new MotorStopScriptMissing(), "./src/test/fixtures/mblock/brainstorming/AS_sol.json");
    }

    @Test
    public void testLBSensorValueEqualsSmell() throws ParsingException, IOException {
        assertThatFinderReports(2, new SensorValueEquals(), "./src/test/fixtures/mblock/brainstorming/LB_bug.json");
    }

    @Test
    public void testLBSensorValueEqualsSol() throws ParsingException, IOException {
        assertThatFinderReports(0, new SensorValueEquals(), "./src/test/fixtures/mblock/brainstorming/LB_sol.json");
    }

    @Test
    public void testALKParallelBoardLaunchScriptMCoreBug() throws ParsingException, IOException {
        assertThatFinderReports(1, new ParallelBoardLaunchScriptMCore(), "./src/test/fixtures/mblock/brainstorming/ALK_bug.json");
    }

    @Test
    public void testALKParallelBoardLaunchScriptMCoreSol() throws ParsingException, IOException {
        assertThatFinderReports(0, new ParallelBoardLaunchScriptMCore(), "./src/test/fixtures/mblock/brainstorming/ALK_sol.json");
    }

    @Test
    public void testMBMotorStopScriptMissingBug() throws ParsingException, IOException {
        assertThatFinderReports(2, new MotorStopScriptMissing(), "./src/test/fixtures/mblock/brainstorming/MB_bug.json");
    }

    @Test
    public void testMBMotorStopScriptMissingSol() throws ParsingException, IOException {
        assertThatFinderReports(0, new MotorStopScriptMissing(), "./src/test/fixtures/mblock/brainstorming/MB_sol.json");
    }

    @Test
    public void testCPTimedLiveLoopSmell() throws ParsingException, IOException {
        assertThatFinderReports(2, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/brainstorming/CP_bug.json");
    }

    @Test
    public void testCPTimedLiveLoopSol() throws ParsingException, IOException {
        assertThatFinderReports(0, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/brainstorming/CP_sol.json");
    }

    @Test
    public void testERDetectRepeatInLoopSmell() throws ParsingException, IOException {
        assertThatFinderReports(2, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/brainstorming/ER_bug.json");
    }

    @Test
    public void testERDetectRepeatInLoopSol() throws ParsingException, IOException {
        assertThatFinderReports(0, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/brainstorming/ER_sol.json");
    }
}
