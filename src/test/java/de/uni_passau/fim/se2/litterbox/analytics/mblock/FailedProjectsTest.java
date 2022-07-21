package de.uni_passau.fim.se2.litterbox.analytics.mblock;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.metric.CodeyCounterMetric;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.metric.MCoreCounterMetric;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.metric.RealCodeyMetric;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.metric.RobotCodeMetric;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.MotorPowerOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FailedProjectsTest implements JsonTest {

    @Test
    public void test102791() throws ParsingException, IOException {
        assertThatFinderReports(0, new CodeyUploadStopTimed(), "./src/test/fixtures/mblock/failed/102791.json");
    }

    @Test
    public void test106760() throws ParsingException, IOException {
        assertThatFinderReports(0, new MotorLowPower(), "./src/test/fixtures/mblock/failed/106760.json");
    }

    @Test
    public void test207868() throws ParsingException, IOException {
        assertThatFinderReports(0, new LoopedStatementNotStopped(), "./src/test/fixtures/mblock/failed/207868.json");
    }

    @Test
    public void test97630() throws ParsingException, IOException {
        assertThatMetricReports(1, new RobotCodeMetric(), "./src/test/fixtures/mblock/failed/97630.json");
        assertThatMetricReports(1, new CodeyCounterMetric(), "./src/test/fixtures/mblock/failed/97630.json");
        assertThatMetricReports(0, new MCoreCounterMetric(), "./src/test/fixtures/mblock/failed/97630.json");
    }

    @Test
    public void test612837() throws ParsingException, IOException {
        assertThatFinderReports(0, new MotorStopScriptMissing(), "./src/test/fixtures/mblock/failed/612837.json");
    }

    @Test
    public void test32912() throws ParsingException, IOException {
        assertThatFinderReports(0, new SensorValueEquals(), "./src/test/fixtures/mblock/failed/32912.json");
    }

    /**
     * Testing for working procedure Injection
     */
    @Test
    public void test105534() throws ParsingException, IOException {
        assertThatFinderReports(0, new CodeyUploadStopTimed(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new LEDOffScriptMissing(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new LoopedStatementNotStopped(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new MatrixOffScriptMissing(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(4, new MotorLowPower(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new MotorStopScriptMissing(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new ParallelBoardLaunchScriptMCore(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new ParallelResourceUse(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new RockyLightOffScriptMissing(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new SensorValueEquals(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/failed/105534.json");
        assertThatFinderReports(0, new MotorPowerOutOfBounds(), "./src/test/fixtures/mblock/failed/105534.json");
    }

    //Not failed, but maybe some issues

    @Test
    public void test48347() throws ParsingException, IOException {
        assertThatFinderReports(1, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/failed/48347.json");
    }

    @Test
    public void test127682() throws ParsingException, IOException {
        assertThatFinderReports(0, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/failed/127682.json");
    }

    @Test
    public void test200252() throws ParsingException, IOException {
        assertThatFinderReports(0, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/failed/200252.json");
    }

    @Test
    public void test304237() throws ParsingException, IOException {
        assertThatFinderReports(5, new TimedStatementInLiveLoop(), "./src/test/fixtures/mblock/failed/304237.json");
        assertThatFinderReports(0, new MatrixOffScriptMissing(), "./src/test/fixtures/mblock/failed/304237.json");
    }

    @Test
    public void test182543() throws ParsingException, IOException {
        assertThatFinderReports(1, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/failed/182542.json");
    }

    @Test
    public void test206950() throws ParsingException, IOException {
        assertThatFinderReports(0, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/failed/206950.json");
    }

    @Test
    public void test225770() throws ParsingException, IOException {
        assertThatFinderReports(0, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/failed/225770.json");
    }

    @Test
    public void test743811() throws ParsingException, IOException {
        assertThatFinderReports(0, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/failed/743811.json");
    }

    @Test
    public void test409990() throws ParsingException, IOException {
        assertThatFinderReports(1, new DetectRepeatInLoop(), "./src/test/fixtures/mblock/failed/409990.json");
    }

    @Test
    public void test214311() throws ParsingException, IOException {
        assertThatMetricReports(1, new RealCodeyMetric(), "./src/test/fixtures/mblock/failed/214311.json");
    }
}
