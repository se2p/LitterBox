package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class SaySoundSynchronisationTest implements JsonTest {

    @Test
    public void testSaySoundSync() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/saysoundsync.json");
        SaySoundSynchronisation saySoundSynchronisation = new SaySoundSynchronisation();
        Set<Issue> reports = saySoundSynchronisation.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testIgnoreWrongSync() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/saysoundTwoFaked.json");
        SaySoundSynchronisation saySoundSynchronisation = new SaySoundSynchronisation();
        Set<Issue> reports = saySoundSynchronisation.check(prog);
        Assertions.assertEquals(1, reports.size());
    }
}