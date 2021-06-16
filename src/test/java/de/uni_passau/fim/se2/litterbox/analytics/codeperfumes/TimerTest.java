package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class TimerTest implements JsonTest {

    @Test
    public void testCounter() throws IOException, ParsingException {
        Program countProg = JsonTest.parseProgram("./src/test/fixtures/goodPractice/counter.json");
        Timer timer = new Timer();
        Set<Issue> reports = timer.check(countProg);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testCounterTwoVarOneCounter() throws IOException, ParsingException {
        Program countProg = JsonTest.parseProgram("./src/test/fixtures/goodPractice/counter2var.json");
        Timer timer = new Timer();
        Set<Issue> reports = timer.check(countProg);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testCounterTwoTrue() throws IOException, ParsingException {
        Program countProg = JsonTest.parseProgram("./src/test/fixtures/goodPractice/counterTwoDifferent.json");
        Timer timer = new Timer();
        Set<Issue> reports = timer.check(countProg);
        Assertions.assertEquals(2, reports.size());
    }
}
