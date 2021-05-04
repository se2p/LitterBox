package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.goodPractice;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.NestedConditionInLoop;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class NestedConditionInLoopTest implements JsonTest {

    @Test
    public void testNestedConInLoop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedConInLoop.json");
        NestedConditionInLoop nestedConditionInLoop = new NestedConditionInLoop();
        Set<Issue> reports = nestedConditionInLoop.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testNestedConInLoopTwoDifferent() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedConInLoopTwo.json");
        NestedConditionInLoop nestedConditionInLoop = new NestedConditionInLoop();
        Set<Issue> reports = nestedConditionInLoop.check(prog);
        Assertions.assertEquals(2, reports.size());
    }
}
