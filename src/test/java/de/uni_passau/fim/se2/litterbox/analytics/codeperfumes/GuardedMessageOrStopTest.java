package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class GuardedMessageOrStopTest implements JsonTest {

    @Test
    public void testSearchWithStop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/searchWithStop.json");
        GuardedMessageOrStop guardedMessageOrStop = new GuardedMessageOrStop();
        Set<Issue> reports = guardedMessageOrStop.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testSearchWithBroadcast() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/searchBroadcast.json");
        GuardedMessageOrStop guardedMessageOrStop = new GuardedMessageOrStop();
        Set<Issue> reports = guardedMessageOrStop.check(prog);
        Assertions.assertEquals(1, reports.size());
    }
}
