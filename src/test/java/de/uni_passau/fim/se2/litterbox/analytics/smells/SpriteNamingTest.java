package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class SpriteNamingTest implements JsonTest {

    @Test
    public void testSpriteNaming() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/spriteNaming.json");
       SpriteNaming parameterName = new SpriteNaming();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(2, reports.size());
    }
}
