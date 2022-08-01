package de.uni_passau.fim.se2.litterbox.analytics.mblock.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSet;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class MBlockSmellsTest implements JsonTest {

    @Test
    public void testCodeyUploadStopTimed() throws ParsingException, IOException {
        assertThatFinderReports(1, new CodeyUploadStopTimed(), "./src/test/fixtures/mblock/test/CodeyUploadStopTimed.json");
    }

    @Test
    public void testCodeyUploadStopTimed_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new CodeyUploadStopTimed(), "./src/test/fixtures/mblock/test/CodeyUploadStopTimed_Sol.json");
    }

    @Test
    public void testDetectRepeatInLoop() throws ParsingException, IOException {
        assertThatFinderReports(2, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/test/DetectRepeatInLoop.json");
    }

    @Test
    public void testDetectRepeatInLoop_2() throws ParsingException, IOException {
        assertThatFinderReports(1, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/test/DetectRepeatInLoop_2.json");
    }

    @Test
    public void testDetectRepeatInLoop_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/test/DetectRepeatInLoop_Sol.json");
    }

    @Test
    public void testParallelResourceUse() throws ParsingException, IOException {
        assertThatFinderReports(9, new ParallelResourceUse(), "./src/test/fixtures/mblock/test/ParallelResourceUse.json");
        Program program = getAST("./src/test/fixtures/mblock/test/ParallelResourceUse.json");
        Set<Issue> issues = runFinder(program, new ParallelBoardLaunchScriptMCore(), false);
        Assertions.assertTrue(issues.stream().allMatch(c -> c instanceof IssueSet));
        Assertions.assertTrue(issues.stream().allMatch(c -> ((IssueSet) c).getScripts().size() == 2));
    }

    @Test
    public void testParallelResourceUse_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new ParallelResourceUse(), "./src/test/fixtures/mblock/test/ParallelResourceUse_Sol.json");
    }

    @Test
    public void testSensorValueEquals() throws ParsingException, IOException {
        assertThatFinderReports(2, new SensorValueEquals(), "./src/test/fixtures/mblock/test/SensorValueEquals.json");
    }

    @Test
    public void testSensorValueEquals_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new SensorValueEquals(), "./src/test/fixtures/mblock/test/SensorValueEquals_Sol.json");
    }

    @Test
    public void testTimedLiveLoop() throws ParsingException, IOException {
        assertThatFinderReports(2, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/test/TimedStatementInLiveLoop.json");
    }

    @Test
    public void testTimedLiveLoop_if_else() throws ParsingException, IOException {
        assertThatFinderReports(2, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/test/TimedStatementInLiveLoop_if_else.json");
    }

    @Test
    public void testTimedLiveLoop_Sol() throws ParsingException, IOException {
        assertThatFinderReports(0, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/test/TimedStatementInLiveLoop_Sol.json");
    }
}
