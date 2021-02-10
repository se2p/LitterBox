package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MessageNamingTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");

        MessageNaming parameterName = new MessageNaming ();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMultiple() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/messageNaming.json");

        MessageNaming parameterName = new MessageNaming ();
        List<Issue> reports = new ArrayList<>(parameterName.check(empty));
        Assertions.assertEquals(3, reports.size());
        Assertions.assertFalse(reports.get(0).isDuplicateOf(reports.get(1)));
        Assertions.assertFalse(reports.get(0).isDuplicateOf(reports.get(2)));
        Assertions.assertTrue(reports.get(2).isDuplicateOf(reports.get(1)));
    }
}
