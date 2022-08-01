package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSet;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class MBlockBugpatternTest implements JsonTest {

    @Test
    public void testLEDOffScriptMissing() throws ParsingException, IOException {
        assertThatFinderReports(4, new LEDOffScriptMissing(), "./src/test/fixtures/mblock/test/LEDOffScriptMissing.json");
    }

    @Test
    public void testLEDOffScriptMissing_Port_Bug() throws ParsingException, IOException {
        assertThatFinderReports(1, new LEDOffScriptMissing(), "./src/test/fixtures/mblock/test/LEDOffScriptMissing_Port_Bug.json");
    }

    @Test
    public void testLEDOffScriptMissing_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new LEDOffScriptMissing(), "./src/test/fixtures/mblock/test/LEDOffScriptMissing_Sol.json");
    }

    @Test
    public void testLoopedNotStopped() throws ParsingException, IOException {
        assertThatFinderReports(10, new LoopedStatementNotStopped(), "./src/test/fixtures/mblock/test/LoopedStatementNotStopped.json");
    }

    @Test
    public void testLoopedNotStopped_MultiStop() throws ParsingException, IOException {
        assertThatFinderReports(5, new LoopedStatementNotStopped(), "./src/test/fixtures/mblock/test/LoopedStatementNotStopped_MultiStop.json");
    }

    @Test
    public void testLoopedNotStopped_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new LoopedStatementNotStopped(), "./src/test/fixtures/mblock/test/LoopedStatementNotStopped_Sol.json");
    }

    @Test
    public void testMatrixOffScriptMissing() throws ParsingException, IOException {
        assertThatFinderReports(4, new MatrixOffScriptMissing(), "./src/test/fixtures/mblock/test/MatrixOffScriptMissing_Ports.json");
    }

    @Test
    public void testMatrixOffScriptMissing_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new MatrixOffScriptMissing(), "./src/test/fixtures/mblock/test/MatrixOffScriptMissing_Sol.json");
    }

    @Test
    public void testMotorLowPower() throws ParsingException, IOException {
        assertThatFinderReports(4, new MotorLowPower(), "./src/test/fixtures/mblock/test/MotorLowPower.json");
    }

    @Test
    public void testMotorStopScriptMissing() throws ParsingException, IOException {
        assertThatFinderReports(4, new MotorStopScriptMissing(), "./src/test/fixtures/mblock/test/MotorStopScriptMissing.json");
    }

    @Test
    public void testMotorStopScriptMissing_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new MotorStopScriptMissing(), "./src/test/fixtures/mblock/test/MotorStopScriptMissing_Sol.json");
    }

    @Test
    public void testParallelBoardLaunchScriptMCore() throws ParsingException, IOException {
        assertThatFinderReports(1, new ParallelBoardLaunchScriptMCore(), "./src/test/fixtures/mblock/test/ParallelBoardLaunchScriptMCore.json");
        Program program = getAST("./src/test/fixtures/mblock/test/ParallelBoardLaunchScriptMCore.json");
        Set<Issue> issues = runFinder(program, new ParallelBoardLaunchScriptMCore(), false);
        Assertions.assertTrue(issues.stream().allMatch(c -> c instanceof IssueSet));
        Assertions.assertTrue(issues.stream().allMatch(c -> ((IssueSet) c).getScripts().size() == 3));
    }

    @Test
    public void testParallelBoardLaunchScriptMCore_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new ParallelBoardLaunchScriptMCore(), "./src/test/fixtures/mblock/test/ParallelBoardLaunchScriptMCore_Sol.json");
    }

    @Test
    public void testRockyLightOffScriptMissing() throws ParsingException, IOException {
        assertThatFinderReports(1, new RockyLightOffScriptMissing(), "./src/test/fixtures/mblock/test/RockyLightOffScriptMissing.json");
    }

    @Test
    public void testRockyLightOffScriptMissing_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new RockyLightOffScriptMissing(), "./src/test/fixtures/mblock/test/RockyLightOffScriptMissing_Sol.json");
    }
}
