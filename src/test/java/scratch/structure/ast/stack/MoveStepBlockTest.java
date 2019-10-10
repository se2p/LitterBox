package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MoveStepBlockTest {

    JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/movesteps.json");
    }

    @Test
    public void readMoveStepsScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof MoveStepBlock)) {
            fail("Result of this fixture should be a movesteps block");
        }

        BasicBlock node = root;
        int count = 0;
        while(node.getNext() != null) {
            count++;
            node = (BasicBlock) node.getNext();
        }

        MoveStepBlock block = (MoveStepBlock) root;
        assertEquals("Three nodes expected", 3, count);
        assertEquals(10, block.getInputValue());
    }
}