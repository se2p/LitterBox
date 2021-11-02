package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UnnecessaryMessageTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryMessage(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUnnecessaryMessage() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnnecessaryMessage(), "./src/test/fixtures/smells/unnecessaryMessage.json");
    }

    @Test
    public void testMessageAlsoSentInInnerPart() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryMessage(), "./src/test/fixtures/smells/messageNotUnnecessary.json");
    }

    @Test
    public void testMessageAlsoSentInInnerPartNested() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryMessage(), "./src/test/fixtures/smells/messageNotUnnecessaryNested.json");
    }
}
