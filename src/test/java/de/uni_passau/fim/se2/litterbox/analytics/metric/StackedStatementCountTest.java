package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class StackedStatementCountTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        StackedStatementCount parameterName = new StackedStatementCount();
        Assertions.assertEquals(0, parameterName.calculateMetric(empty));
    }

    @Test
    public void testStackedTTS() throws IOException, ParsingException {
        Program unusedProc = JsonTest.parseProgram("./src/test/fixtures/metrics/stackedTTS.json");
        StackedStatementCount parameterName = new StackedStatementCount();
        Assertions.assertEquals(1, parameterName.calculateMetric(unusedProc));
    }
}
