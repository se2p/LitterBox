package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CostumeNameExtractionTest implements JsonTest {

    @Test
    public void testCostumeNameExtraction() throws IOException, ParsingException {
        List<String> list = new ArrayList<>();
        list.add("costume1");
        list.add("costume2");
        list.add("andie-a");
        list.add("andie-b");
        list.add("andie-c");
        list.add("andie-d");
        assertThatExtractionReports(list, new CostumeNameExtraction(), "./src/test/fixtures/extraction/multiVariable.json");
    }
}
