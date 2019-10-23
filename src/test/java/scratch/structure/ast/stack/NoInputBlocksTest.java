package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.cap.DeleteCloneBlock;
import utils.JsonParser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NoInputBlocksTest {
    private JsonNode script;
    private List<String> opcodes;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/ifonedgebounce.json");
        opcodes = Arrays.asList("motion_ifonedgebounce",
                "looks_nextcostume",
                "looks_nextbackdrop",
                "looks_cleargraphiceffects",
                "looks_show",
                "looks_hide",
                "sound_stopallsounds",
                "sound_cleareffects",
                "sensing_resettimer"
                );
    }

    @Test
    public void readNoInputBlockScript() {
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
        Iterator<String> it = opcodes.iterator();
        assertEquals(it.next(), block.getOpcode());

        Stackable next = root.getNext();
        while(next != null && it.hasNext()) {
            assertEquals(it.next(), block.getOpcode());
            next = next.getNext();
        }
    }
}

