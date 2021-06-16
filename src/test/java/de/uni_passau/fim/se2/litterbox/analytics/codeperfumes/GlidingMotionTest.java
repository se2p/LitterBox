package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class GlidingMotionTest implements JsonTest {

    @Test
    public void testGlidingMotion() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/glidingMotionTwo.json");
        GlidingMotion glidingMotion = new GlidingMotion();
        Set<Issue> reports = glidingMotion.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testGlidingMotionIgnoreWrong() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/glidingMotionOneFake.json");
        GlidingMotion glidingMotion = new GlidingMotion();
        Set<Issue> reports = glidingMotion.check(prog);
        Assertions.assertEquals(1, reports.size());
    }
}
