package scratch.structure.ast.cblock;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.hat.WhenFlagClickedBlock;
import scratch.structure.ast.inputs.Literal;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RepeatBlockTest {
    JsonNode script;

    @Before
    public void setUp() throws Exception {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/repeatMoveScript.json");
    }

    @Test
    public void readRepeatMoveScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof WhenFlagClickedBlock)) {
            fail("Result of this fixture should be a deleteClone block");
        }

        ScratchBlock node = root;

        WhenFlagClickedBlock block = (WhenFlagClickedBlock) root;
        RepeatBlock repeat = (RepeatBlock) root.getNext();
        assertEquals("control_repeat", repeat.getOpcode());
        assertEquals("10", ((Literal) repeat.getSlot().getPrimary()).getValue());
        assertEquals("motion_movesteps", repeat.getSubstack().getSubstack().getOpcode()); //FIXME Rename methods


    }
}
