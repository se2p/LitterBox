package de.uni_passau.fim.se2.litterbox.analytics.goodPractice;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.goodpractices.NestedConditions;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class NestedConditionsTest implements JsonTest {

    @Test
    public void testNestedConditionEasy() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedConditionEasy.json");
        NestedConditions nestedConditions = new NestedConditions();
        Set<Issue> reports = nestedConditions.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testNestedConditionsDeep() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/nestedConditionsDeep.json");
        NestedConditions nestedConditions = new NestedConditions();
        Set<Issue> reports = nestedConditions.check(prog);
        Assertions.assertEquals(1, reports.size());
    }
}
