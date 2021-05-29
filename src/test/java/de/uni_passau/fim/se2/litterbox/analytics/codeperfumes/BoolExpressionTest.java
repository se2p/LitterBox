package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MessageNeverReceived;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.BoolExpression;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        List<Issue> reports = new ArrayList<>(bool.check(boolProg));
        Assertions.assertEquals(7, reports.size());
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(1)));
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(2)));
        Assertions.assertTrue(reports.get(1).isDuplicateOf(reports.get(2)));
    }
}
