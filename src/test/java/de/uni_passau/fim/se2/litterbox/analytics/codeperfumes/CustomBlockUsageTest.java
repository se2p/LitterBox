package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class CustomBlockUsageTest implements JsonTest {

    @Test
    public void testCustomBlock() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/customblock.json");
        CustomBlockUsage customBlockUsage = new CustomBlockUsage();
        Set<Issue> reports = customBlockUsage.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testIgnoreIncomplete() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/callWithoutDefinition.json");
        CustomBlockUsage customBlockUsage = new CustomBlockUsage();
        Set<Issue> reports = customBlockUsage.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}
