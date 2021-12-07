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
        int positionCat = 0;
        int positionAbby = 0;
        if (pathContextsPerSprite.get(0).getName().equals("cat") && pathContextsPerSprite.get(1).getName().equals("abby")){
            positionAbby = 1;
        } else if (pathContextsPerSprite.get(1).getName().equals("cat") && pathContextsPerSprite.get(0).getName().equals("abby")) {
            positionCat = 1;
        } else {
            fail();
        }

        // Sprite cat
        ProgramFeatures cat = pathContextsPerSprite.get(positionCat);
        assertEquals("cat", cat.getName());
        assertEquals(3, cat.getFeatures().size());
        assertEquals("NumberLiteral,625791294,StringLiteral",
                cat.getFeatures().get(0).toString());
        assertEquals("NumberLiteral,1493538624,Show",
                cat.getFeatures().get(1).toString());
        assertEquals("StringLiteral,-547448667,Show",
                cat.getFeatures().get(2).toString());

        // Sprite abby
        ProgramFeatures abby = pathContextsPerSprite.get(positionAbby);
        assertEquals("abby", abby.getName());
        assertEquals(1, abby.getFeatures().size());
        assertEquals("GreenFlag,-2069003229,StringLiteral",
                abby.getFeatures().get(0).toString());
    }
}
