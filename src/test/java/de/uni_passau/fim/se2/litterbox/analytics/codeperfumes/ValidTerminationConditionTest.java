package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.solutionpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.ValidTerminationCondition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class ValidTerminationConditionTest implements JsonTest {

    @Test
    public void testInvalidTerminationCondition() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/missingTermination/missingTermination.json");
        ValidTerminationCondition validTerminationCondition = new ValidTerminationCondition();
        Set<Issue> reports = validTerminationCondition.check(prog);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testOneValidTerminationCon() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/oneValidTermCon.json");
        ValidTerminationCondition validTerminationCondition = new ValidTerminationCondition();
        Set<Issue> reports = validTerminationCondition.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testValidTermConWithVariable() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/validTermWithVariable.json");
        ValidTerminationCondition validTerminationCondition = new ValidTerminationCondition();
        Set<Issue> reports = validTerminationCondition.check(prog);
        Assertions.assertEquals(1, reports.size());
    }
}
