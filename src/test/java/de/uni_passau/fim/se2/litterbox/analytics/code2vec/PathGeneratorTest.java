package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PathGeneratorTest implements JsonTest {

    @Test
    void testGeneratePaths() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator generator = new PathGenerator(program, 8);
        ArrayList<ProgramFeatures> pathContextsPerSprite = generator.generatePaths();
        assertEquals(2, pathContextsPerSprite.size());
        // Sprite cat
        assertEquals("cat", pathContextsPerSprite.get(0).getName());
        assertEquals(3, pathContextsPerSprite.get(0).getFeatures().size());
        assertEquals("NumberLiteral,625791294,StringLiteral",
                pathContextsPerSprite.get(0).getFeatures().get(0).toString());
        assertEquals("NumberLiteral,1493538624,Show",
                pathContextsPerSprite.get(0).getFeatures().get(1).toString());
        assertEquals("StringLiteral,-547448667,Show",
                pathContextsPerSprite.get(0).getFeatures().get(2).toString());

        // Sprite abby
        assertEquals("abby", pathContextsPerSprite.get(1).getName());
        assertEquals(1, pathContextsPerSprite.get(1).getFeatures().size());
        assertEquals("GreenFlag,-2069003229,StringLiteral",
                pathContextsPerSprite.get(1).getFeatures().get(0).toString());
    }
}
