package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.BoolExpression;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class BoolExpressionTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        BoolExpression bool = new BoolExpression();
        Set<Issue> reports = bool.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testBooleanExpressions() throws IOException, ParsingException {
        Program boolProg =  JsonTest.parseProgram("./src/test/fixtures/goodPractice/boolExpressions.json");
        BoolExpression bool = new BoolExpression();
        Set<Issue> reports = bool.check(boolProg);
        Assertions.assertEquals(7, reports.size());
    }
}
