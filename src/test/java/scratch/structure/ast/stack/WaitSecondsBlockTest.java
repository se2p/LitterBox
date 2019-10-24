package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.inputs.Literal;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WaitSecondsBlockTest {

    JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/waitseconds.json");
    }

    @Test
    public void testStructure() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof WaitSecondsBlock)) {
            fail("Result of this fixture should be a waitseconds block");
        }

        Extendable node = (Extendable) root;
        int count = 0;
        while (node.getNext() != null) {
            count++;
            node = (Extendable) node.getNext();
        }

        assertEquals("Two nodes expected", 2, count);
    }

    @Test
    public void testIntInput() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof WaitSecondsBlock)) {
            fail("Result of this fixture should be a waitseconds block");
        }
        WaitSecondsBlock block = (WaitSecondsBlock) root;
        Literal primary = (Literal) block.getSlot().getPrimary();
        String value = primary.getValue();
        assertEquals("1", value);
    }

//    @Test TODO update this test as soon as we have variable blocks
//    public void testVariableInput() {
//        Ast ast = new Ast();
//        ast.parseScript(script);
//
//        ScratchBlock root = ast.getRoot();
//        if (!(root instanceof WaitSecondsBlock)) {
//            fail("Result of this fixture should be a waitseconds block");
//        }
//        WaitSecondsBlock block = (WaitSecondsBlock) root.getNext();
//
//        assertEquals("`jEk@4|i[#Fk?(8x)AV.-my variable", block.getInputID());
//    }
}

