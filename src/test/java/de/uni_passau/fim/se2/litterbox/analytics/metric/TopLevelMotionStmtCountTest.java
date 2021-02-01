package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TopLevelMotionStmtCountTest implements JsonTest {

    @Test
    public void testAll() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/metrics/allMotionBlocks.json");
        TopLevelMotionStmtCount parameterName = new TopLevelMotionStmtCount();
        Assertions.assertEquals(15, parameterName.calculateMetric(empty));
    }

}
