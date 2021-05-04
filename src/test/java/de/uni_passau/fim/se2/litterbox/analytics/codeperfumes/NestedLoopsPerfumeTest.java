package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.NestedLoopsPerfume;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class NestedLoopsPerfumeTest implements JsonTest {

    @Test
    public void testNestedLoopsTwo() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedLoopsTwo.json");
        NestedLoopsPerfume nestedLoopsPerfume = new NestedLoopsPerfume();
        Set<Issue> reports = nestedLoopsPerfume.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testNestedLoopsThree() throws IOException, ParsingException {

        //Only outer loop should be added as issue, no matter how deep the loops are
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedLoopsThree.json");
        NestedLoopsPerfume nestedLoopsPerfume = new NestedLoopsPerfume();
        Set<Issue> reports = nestedLoopsPerfume.check(prog);
        Assertions.assertEquals(1, reports.size());
    }
}
