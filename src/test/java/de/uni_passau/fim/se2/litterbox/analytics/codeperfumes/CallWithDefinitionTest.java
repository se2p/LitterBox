package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.solutionpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.CallWithDefinition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class CallWithDefinitionTest implements JsonTest {

    @Test
    public void testCallWithDef() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/CallWithDef.json");
        CallWithDefinition callWithDefinition = new CallWithDefinition();
        Set<Issue> reports = callWithDefinition.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testCorrectCustomBlockUsage() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/customblock.json");
        CallWithDefinition callWithDefinition = new CallWithDefinition();
        Set<Issue> reports = callWithDefinition.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testCallWithoutDefinition() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/callWithoutDefinition.json");
        CallWithDefinition callWithDefinition = new CallWithDefinition();
        Set<Issue> reports = callWithDefinition.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}
