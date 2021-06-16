package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class CorrectBroadcastTest implements JsonTest {

    @Test
    public void testCorrectBroadcast() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/correctBroadcast.json");
        CorrectBroadcast correctBroadcast = new CorrectBroadcast();
        Set<Issue> reports = correctBroadcast.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testCorrectBroadcastAndWait() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/coordinationBRandWait.json");
        CorrectBroadcast correctBroadcast = new CorrectBroadcast();
        Set<Issue> reports = correctBroadcast.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testMissingReceiver() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/messageNeverReceivedDouble.json");
        CorrectBroadcast correctBroadcast = new CorrectBroadcast();
        Set<Issue> reports = correctBroadcast.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}
