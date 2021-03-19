package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class UnnecessaryWaitTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        UnnecessaryWait parameterName = new UnnecessaryWait();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testUnnecessaryWaitProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/unnecessaryWait.json");
        UnnecessaryWait parameterName = new UnnecessaryWait();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(1, reports.size());
    }
}
