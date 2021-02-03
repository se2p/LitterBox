package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class InsideControlPenStmtCountTest implements JsonTest {

    @Test
    public void testAllLoose() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allPenBlocks.json");
        InsideControlPenStmtCount parameterName = new InsideControlPenStmtCount();
        Assertions.assertEquals(0, parameterName.calculateMetric(empty));
    }

    @Test
    public void testAll() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allPenInsideControl.json");
        InsideControlPenStmtCount parameterName = new InsideControlPenStmtCount();
        Assertions.assertEquals(9, parameterName.calculateMetric(empty));
    }
}
