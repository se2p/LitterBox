package de.uni_passau.fim.se2.litterbox.analytics.scratchblocks;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramScratchBlocksAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ProgramScratchBlocksAnalyzerTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        ProgramScratchBlocksAnalyzer scratchBlocksAnalyzer = new ProgramScratchBlocksAnalyzer();
        String scratchBlocks = scratchBlocksAnalyzer.analyze(empty);
        Assert.assertEquals(scratchBlocks, "//Sprite: Stage\n//Sprite: Sprite1");
    }

    @Test
    public void testNonEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/allBlocks.json");
        ProgramScratchBlocksAnalyzer scratchBlocksAnalyzer = new ProgramScratchBlocksAnalyzer();
        String scratchBlocks = scratchBlocksAnalyzer.analyze(empty);
        Assert.assertTrue(scratchBlocks.contains("when [space v] key pressed\nturn right (15) degrees"));
    }
}
