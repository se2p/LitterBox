package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CostumeCountTest  implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        CostumeCount parameterName = new CostumeCount();
        Assertions.assertEquals(2, parameterName.calculateMetric(empty));
    }

    @Test
    public void testMultipleSprites() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/metrics/multipleSpritesBackdrops.json");
        CostumeCount parameterName = new CostumeCount();
        Assertions.assertEquals(4, parameterName.calculateMetric(empty));
    }

}
