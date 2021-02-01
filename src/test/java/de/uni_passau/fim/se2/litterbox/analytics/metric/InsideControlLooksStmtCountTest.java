package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class InsideControlLooksStmtCountTest implements JsonTest {

    @Test
    public void testAllLoose() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allLooksBlocks.json");
        InsideControlLooksStmtCount parameterName = new InsideControlLooksStmtCount();
        Assertions.assertEquals(0, parameterName.calculateMetric(empty));
    }

    @Test
    public void testAll() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allLooksInsideControl.json");
        InsideControlLooksStmtCount parameterName = new InsideControlLooksStmtCount();
        Assertions.assertEquals(18, parameterName.calculateMetric(empty));
    }
}
