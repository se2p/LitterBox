package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpriteNameExtractionTest implements JsonTest {

    @Test
    public void testSpriteNameExtraction() throws IOException, ParsingException {
        List<String> list = new ArrayList<>();
        list.add("Sprite1");
        list.add("Andie");
        assertThatExtractionReports(list, new SpriteNameExtraction(), "./src/test/fixtures/extraction/multiVariable.json");
    }
}
