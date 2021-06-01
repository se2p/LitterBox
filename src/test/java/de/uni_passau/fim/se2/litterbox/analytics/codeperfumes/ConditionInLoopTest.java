package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConditionInLoopTest implements JsonTest {

    @Test
    public void testNestedConInLoop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedConInLoop.json");
        ConditionInLoop conditionInLoop = new ConditionInLoop();
        Set<Issue> reports = conditionInLoop.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testNestedConInLoopTwoDifferent() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedConInLoopTwo.json");
        ConditionInLoop conditionInLoop = new ConditionInLoop();
        Set<Issue> reports = conditionInLoop.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testNestedConInLoopDuplicates() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/multipleNestedConditionInLoop.json");
        ConditionInLoop conditionInLoop = new ConditionInLoop();
        List<Issue> reports = new ArrayList<>(conditionInLoop.check(prog));
        Assertions.assertEquals(11, reports.size());
        for (int i = 0; i < reports.size(); i++) {
            for (int j = i + 1; j < reports.size(); j++) {
                Assertions.assertTrue(reports.get(i).isDuplicateOf(reports.get(j)));
            }
        }
    }
}
