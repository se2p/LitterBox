package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UselessBlocksTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UselessBlocks(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testSpriteExclusive() throws IOException, ParsingException {
        assertThatFinderReports(48, new UselessBlocks(), "./src/test/fixtures/smells/allSpriteExclusiveBlocksInStage.json");
    }

    @Test
    public void testStageExclusive() throws IOException, ParsingException {
        assertThatFinderReports(1, new UselessBlocks(), "./src/test/fixtures/smells/allStageExclusiveBlocksInSprite.json");
    }
}
