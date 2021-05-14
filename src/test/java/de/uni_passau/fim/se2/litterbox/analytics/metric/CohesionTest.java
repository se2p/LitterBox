package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CohesionTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws ParsingException, IOException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        Cohesion cohesion = new Cohesion();
        Assertions.assertEquals(0, cohesion.calculateMetric(empty));
    }
}
