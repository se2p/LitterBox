package scratch.structure.ast.hat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WhenSpriteClickedTest {

    private JsonNode script;

    @Before
    public void setup() {
        script = Utils.parseScript("./src/test/java/scratch/structure/ast/fixtures/whenspriteclicked.json");
    }

    @Test
    public void testWhenSpriteClickedScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof WhenSpriteClickedBlock)) {
            fail("Result of this fixture should be a WhenSpriteClicked block");
        }

        BasicBlock node = root;
        int count = 1;
        while (node.getNext() != null) {
            count++;
            node = (BasicBlock) node.getNext();
        }

        WhenSpriteClickedBlock block = (WhenSpriteClickedBlock) root;
        assertEquals("One node expected", 1, count);
        assertEquals("event_whenthisspriteclicked", block.getOpcode());
    }
}