package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class SearchTest implements JsonTest {

    @Test
    public void testSearchWithStop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/searchWithStop.json");
        Search search = new Search();
        Set<Issue> reports = search.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testSearchWithBroadcast() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/searchBroadcast.json");
        Search search = new Search();
        Set<Issue> reports = search.check(prog);
        Assertions.assertEquals(1, reports.size());
    }
}
