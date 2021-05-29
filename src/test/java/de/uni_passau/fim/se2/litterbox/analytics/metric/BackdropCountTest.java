package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BackdropCountTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        BackdropCount parameterName = new BackdropCount();
        Assertions.assertEquals(1, parameterName.calculateMetric(empty));
    }

    @Test
    public void testMultipleBackdrops() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/metrics/multipleSpritesBackdrops.json");
        BackdropCount parameterName = new BackdropCount();
        Assertions.assertEquals(2, parameterName.calculateMetric(empty));
    }
}
