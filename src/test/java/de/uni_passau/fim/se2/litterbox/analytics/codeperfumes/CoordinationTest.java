package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.Coordination;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class CoordinationTest implements JsonTest {

    @Test
    public void testCoordination() throws IOException, ParsingException {
        Program coordProg = JsonTest.parseProgram("./src/test/fixtures/goodPractice/coordinationBoth.json");
        Coordination coordination = new Coordination();
        Set<Issue> reports = coordination.check(coordProg);
        Assertions.assertEquals(1, reports.size());
    }
 }
