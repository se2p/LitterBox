package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class ImmediateDeleteCloneAfterBroadcastTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        ImmediateDeleteCloneAfterBroadcast parameterName = new ImmediateDeleteCloneAfterBroadcast();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testImmediateDeleteCloneAfterBroadcast() throws IOException, ParsingException {
        Program illegalParameter = JsonTest.parseProgram("./src/test/fixtures/bugpattern/immediateDeleteCloneAfterBroadcast.json");
        ImmediateDeleteCloneAfterBroadcast parameterName = new ImmediateDeleteCloneAfterBroadcast();
        Set<Issue> reports = parameterName.check(illegalParameter);
        Assertions.assertEquals(1, reports.size());
    }
}

