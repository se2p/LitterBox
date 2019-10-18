package scratch.structure.ast.hat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WhenStartAsCloneTest {
    JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/whenstartasclone.json");
    }

    @Test
    public void testWhenStartAsCloneScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof WhenStartAsCloneBlock)) {
            fail("Result of this fixture should be a WhenStartAsClone block");
        }

        ScratchBlock node = root;
        int count = 1;
        while (node.getNext() != null) {
            count++;
            node = (ScratchBlock) node.getNext();
        }

        WhenStartAsCloneBlock block = (WhenStartAsCloneBlock) root;
        assertEquals("One node expected", 1, count);
        assertEquals("control_start_as_clone", block.getOpcode());
    }
}
