package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.solutionpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.UsefulPositionCheck;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class UsefulPositionCheckTest implements JsonTest {

    @Test
    public void testPositionCheck() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/positionEqualsCheck.json");
        UsefulPositionCheck usefulPositionCheck = new UsefulPositionCheck();
        Set<Issue> reports = usefulPositionCheck.check(prog);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testProblematicPositionCheck() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/positionEqualsCheckStrict.json");
        UsefulPositionCheck usefulPositionCheck = new UsefulPositionCheck();
        Set<Issue> reports = usefulPositionCheck.check(prog);
        Assertions.assertEquals(0, reports.size());
    }

}
