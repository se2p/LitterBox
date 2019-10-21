package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.inputs.Literal;
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

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof MoveStepBlock)) {
            fail("Result of this fixture should be a movesteps block");
        }

        ScratchBlock node = root;
        int count = 0;
        while (node.getNext() != null) {
            count++;
            node = (ScratchBlock) node.getNext();
        }

        MoveStepBlock block = (MoveStepBlock) root;
        Literal primary = (Literal) block.getSlot().getPrimary();
        String value = primary.getValue();
        assertEquals("Three nodes expected", 3, count);
        assertEquals("10", value);
    }
}