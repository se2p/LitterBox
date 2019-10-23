package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.cap.DeleteCloneBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IfOnEdgeBounceBlockTest {
    private JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/ifonedgebounce.json");
    }

    @Test
    public void readDeleteCloneScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof IfOnEdgeBounceBlock)) {
            fail("Result of this fixture should be a deleteClone block");
        }

        ScratchBlock node = root;
        int count = 1;
        while(node.getNext() != null) {
            count++;
            node = (ScratchBlock) node.getNext();
        }

        IfOnEdgeBounceBlock block = (IfOnEdgeBounceBlock) root;
        assertEquals("One node expected", 1, count);
        assertEquals("motion_ifonedgebounce", block.getOpcode());
    }
}

