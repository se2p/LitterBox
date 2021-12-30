package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PathGeneratorTest implements JsonTest {

    final String[] expectedLeafs = {"loudness", "10", "Hello!", "left-right", "pitch", "100", "draggable",
            "color", "0", "1", "forward", "front", "number", "Size", "1", "2", "log", "year"};

    @Test
    public void testGeneratePaths() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator generator = new PathGenerator(program, 8);
        ArrayList<ProgramFeatures> pathContextsPerSprite = generator.generatePaths();
        assertEquals(2, pathContextsPerSprite.size());
        int positionCat = 0;
        int positionAbby = 0;
        if (pathContextsPerSprite.get(0).getName().equals("cat")
                && pathContextsPerSprite.get(1).getName().equals("abby")){
            positionAbby = 1;
        } else if (pathContextsPerSprite.get(1).getName().equals("cat")
                && pathContextsPerSprite.get(0).getName().equals("abby")) {
            positionCat = 1;
        } else {
            fail();
        }

        // Sprite cat
        ProgramFeatures cat = pathContextsPerSprite.get(positionCat);
        assertEquals("cat", cat.getName());
        assertEquals(3, cat.getFeatures().size());
        assertEquals("39,625791294,Hi!",
                cat.getFeatures().get(0).toString());
        assertEquals("39,1493538624,Show",
                cat.getFeatures().get(1).toString());
        assertEquals("Hi!,-547448667,Show",
                cat.getFeatures().get(2).toString());

        // Sprite abby
        ProgramFeatures abby = pathContextsPerSprite.get(positionAbby);
        assertEquals("abby", abby.getName());
        assertEquals(1, abby.getFeatures().size());
        assertEquals("GreenFlag,-2069003229,Hello!",
                abby.getFeatures().get(0).toString());
    }

    @Test
    public void testGeneratePathsWithDifferentTokens() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/allChangeableTokens.json");
        PathGenerator generator = new PathGenerator(program, 8);
        ArrayList<String> tokens = generator.getAllLeafs();
        assertArrayEquals(expectedLeafs, tokens.toArray());
    }
}
