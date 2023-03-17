package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListNameExtractionTest implements JsonTest {

    @Test
    public void testListNameExtraction() throws IOException, ParsingException {
        List<String> list = new ArrayList<>();
        list.add("testGlobal");
        list.add("testLocal");
        assertThatExtractionReports(list, new ListNameExtraction(), "./src/test/fixtures/extraction/multiList.json");
    }
}
