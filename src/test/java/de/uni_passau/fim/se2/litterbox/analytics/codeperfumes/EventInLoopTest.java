package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.solutionpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.EventInLoop;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class EventInLoopTest implements JsonTest {

    @Test
    public void testEventInLoop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/eventInLoop.json");
        EventInLoop eventInLoop = new EventInLoop();
        Set<Issue> reports = eventInLoop.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testTwoEventsInLoop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/eventInLoopTwo.json");
        EventInLoop eventInLoop = new EventInLoop();
        Set<Issue> reports = eventInLoop.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testMissingSensingForEvent() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingLoopSensingMultiple.json");
        EventInLoop eventInLoop = new EventInLoop();
        Set<Issue> reports = eventInLoop.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}
