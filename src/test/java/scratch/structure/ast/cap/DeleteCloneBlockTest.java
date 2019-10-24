package scratch.structure.ast.cap;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DeleteCloneBlockTest {
    private JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/deleteclone.json");
    }

    @Test
    public void readDeleteCloneScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof DeleteCloneBlock)) {
            fail("Result of this fixture should be a deleteClone block");
        }

        //TODO Improve
        DeleteCloneBlock block = (DeleteCloneBlock) root;
        assertEquals("control_delete_this_clone", block.getOpcode());
    }
}

