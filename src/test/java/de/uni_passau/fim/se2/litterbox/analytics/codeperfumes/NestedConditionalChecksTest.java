package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class NestedConditionalChecksTest implements JsonTest {

    @Test
    public void testNestedConditionEasy() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedConditionEasy.json");
        NestedConditionalChecks nestedConditionalChecks = new NestedConditionalChecks();
        Set<Issue> reports = nestedConditionalChecks.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testNestedConditionsDeep() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedConditionsDeep.json");
        NestedConditionalChecks nestedConditionalChecks = new NestedConditionalChecks();
        Set<Issue> reports = nestedConditionalChecks.check(prog);
        Assertions.assertEquals(1, reports.size());
    }
}
