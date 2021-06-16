package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class ParallelisationTest implements JsonTest {

    @Test
    public void testTwoParallelization() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/parallelFlagAndKey.json");
        Parallelisation parallelisation = new Parallelisation();
        Set<Issue> reports = parallelisation.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testIgnoreWrongParallelization() throws  IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/oneFakeParallel.json");
        Parallelisation parallelisation = new Parallelisation();
        Set<Issue> reports = parallelisation.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testSixParallelization() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/sixParallelThreeFake.json");
        Parallelisation parallelisation = new Parallelisation();
        Set<Issue> reports = parallelisation.check(prog);
        Assertions.assertEquals(6, reports.size());
    }
}
