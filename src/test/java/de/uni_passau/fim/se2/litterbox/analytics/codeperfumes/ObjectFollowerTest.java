package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class ObjectFollowerTest implements JsonTest {

    @Test
    public void testObjectFollowerTwo() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/objectFollowerTwo.json");
        ObjectFollower objectFollower = new ObjectFollower();
        Set<Issue> reports = objectFollower.check(prog);
        Assertions.assertEquals(2, reports.size());
    }
}
