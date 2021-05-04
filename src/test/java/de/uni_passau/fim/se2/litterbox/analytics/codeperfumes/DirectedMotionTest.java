package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.DirectedMotion;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class DirectedMotionTest implements JsonTest {

    @Test
    public void testDirectedMotion() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/directedMotion.json");
        DirectedMotion directedMotion = new DirectedMotion();
        Set<Issue> reports = directedMotion.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testDirectedMotionOneFaked() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/twoDirectedMotionOneFake.json");
        DirectedMotion directedMotion = new DirectedMotion();
        Set<Issue> reports = directedMotion.check(prog);
        Assertions.assertEquals(2, reports.size());
    }
}
