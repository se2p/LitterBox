package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern.AmbientLightOutOfBounds;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AmbientLightOutOfBoundsTest implements JsonTest {
    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new AmbientLightOutOfBounds(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void ambientOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(6, new AmbientLightOutOfBounds(), "./src/test/fixtures/mblock/test/ambientLightOutOfBounds.json");
    }

    @Test
    public void ambientPortOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(6, new AmbientLightOutOfBounds(), "./src/test/fixtures/mblock/test/ambientLightPortOutOfBounds.json");
    }

    @Test
    public void ambientmBotOutOfBounds() throws ParsingException, IOException {
        assertThatFinderReports(3, new AmbientLightOutOfBounds(), "./src/test/fixtures/mblock/test/ambientLightmBotOutOfBounds.json");
    }
}
