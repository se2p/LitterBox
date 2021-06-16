package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class ListUsageTest implements JsonTest {

    @Test
    public void testListUsedFiveTimes() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/listUsedFiveTimes.json");
        ListUsage listUsage = new ListUsage();
        Set<Issue> reports = listUsage.check(prog);
        Assertions.assertEquals(5, reports.size());
    }

    @Test
    public void testListUnused() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/listUnused.json");
        ListUsage listUsage = new ListUsage();
        Set<Issue> reports = listUsage.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}
