package scratch.structure.ast.dynamicMenu;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.bool.KeyPressedBlock;
import scratch.structure.ast.cblock.IfBlock;
import scratch.structure.ast.hat.WhenFlagClickedBlock;
import utils.JsonParser;

import static junit.framework.TestCase.assertEquals;

public class KeyOptionsBlockTest {

    private JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/keyMenuOption.json");
    }

    @Test
    public void testKeyOptionsBlock(){
        Ast ast = new Ast();
        ast.parseScript(script);
        WhenFlagClickedBlock root = (WhenFlagClickedBlock) ast.getRoot();
        IfBlock ifBlock = (IfBlock) root.getNext();
        KeyPressedBlock keyPressed = (KeyPressedBlock) ifBlock.getSlot().getPrimary();
        assertEquals("space", ((KeyOptionsBlock) keyPressed.getKeyOption().getPrimary()).getKeyOption());

    }
}
