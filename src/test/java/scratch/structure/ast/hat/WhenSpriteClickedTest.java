package scratch.structure.ast.hat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WhenSpriteClickedTest {

    private JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/whenspriteclicked.json");
    }

    @Test
    public void testWhenSpriteClickedScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof WhenSpriteClickedBlock)) {
            fail("Result of this fixture should be a WhenSpriteClicked block");
        }

        ScratchBlock node = root;
        int count = 1;
        while (node.getNext() != null) {
            count++;
            node = (ScratchBlock) node.getNext();
        }

        WhenSpriteClickedBlock block = (WhenSpriteClickedBlock) root;
        assertEquals("One node expected", 1, count);
        assertEquals("event_whenthisspriteclicked", block.getOpcode());
    }
}