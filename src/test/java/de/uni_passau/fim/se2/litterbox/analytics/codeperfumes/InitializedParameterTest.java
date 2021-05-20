package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.InitializedParameter;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class InitializedParameterTest implements JsonTest {

    @Test
    public void testInizializedParameter() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/initializedParameter.json");
        InitializedParameter initializedParameter = new InitializedParameter();
        Set<Issue> reports = initializedParameter.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testOrphanedParameter() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/orphanedParameter.json");
        InitializedParameter initializedParameter = new InitializedParameter();
        Set<Issue> reports = initializedParameter.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}