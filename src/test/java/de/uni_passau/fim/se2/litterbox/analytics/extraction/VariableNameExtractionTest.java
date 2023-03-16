package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VariableNameExtractionTest implements JsonTest {

    @Test
    public void testSimpleBackdropSwitchAndEvent() throws IOException, ParsingException {
        List<String> list = new ArrayList<>();
        list.add("my variable");
        list.add("blub");
        assertThatExtractionReports(list, new VariableNameExtraction(), "./src/test/fixtures/extraction/multiVariable.json");
    }
}
