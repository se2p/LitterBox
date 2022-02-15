package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GeneratePathTaskTest implements JsonTest{

    final String OUTPUT_TEST = "cat 39,625791294,Hi!" +
            " 39,1493538624,Show " +
            "Hi!,-547448667,Show\n" +
            "abby GreenFlag,-2069003229,Hello!";

    final String OUTPUT_TEST_SPRITES_POSITION_CHANGED = "abby GreenFlag,-2069003229,Hello!\n" +
            "cat 39,625791294,Hi!" +
            " 39,1493538624,Show " +
            "Hi!,-547448667,Show";

    @Test
    void testCreateContextForCode2Vec() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        GeneratePathTask generatePathTask = new GeneratePathTask(program, 8);
        String pathContextForCode2Vec = generatePathTask.createContextForCode2Vec();
        boolean outputCheck = pathContextForCode2Vec.equals(OUTPUT_TEST) || pathContextForCode2Vec.equals(OUTPUT_TEST_SPRITES_POSITION_CHANGED);
        assertTrue(outputCheck);
    }
}
