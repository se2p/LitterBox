package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SayHelloBlockCountTest implements JsonTest {

    @Test
    public void testAll() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/bugpattern/missingPenDown.json");
        SayHelloBlockCount counter = new SayHelloBlockCount();
        Assertions.assertEquals(2, counter.calculateMetric(program));
    }
}
