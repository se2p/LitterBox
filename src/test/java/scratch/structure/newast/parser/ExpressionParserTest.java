package scratch.structure.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.newast.model.expression.num.Number;
import scratch.newast.parser.ExpressionParser;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;

public class ExpressionParserTest {
    private static JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/movesteps.json");
    }

    @Test
    public void testParseNumber() {
        JsonNode inputs = script.get("EU(l=G6)z8NGlJFcx|fS").get("inputs");
        Number result = ExpressionParser.parseNumber(inputs, 0);
        assertEquals("10.0", String.valueOf(result.getValue()));
    }
}
