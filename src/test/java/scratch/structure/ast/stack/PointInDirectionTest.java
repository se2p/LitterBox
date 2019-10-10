package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PointInDirectionTest {

    JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/pointindirection.json");
    }

    @Test
    public void testStructure() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof TurnDegreesBlock)) {
            fail("Result of this fixture should be a pointindirection block");
        }

        BasicBlock node = root;
        int count = 0;
        while(node.getNext() != null) {
            count++;
            node = (BasicBlock) node.getNext();
        }
        assertEquals("Two nodes expected", 2, count);
    }

    @Test
    public void testIntInput() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof TurnDegreesBlock)) {
            fail("Result of this fixture should be a pointindirection block");
        }
        TurnDegreesBlock block = (TurnDegreesBlock) root;
        assertEquals(90, block.getInputValue());
    }

    @Test
    public void testVariableInput() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof TurnDegreesBlock)) {
            fail("Result of this fixture should be a pointindirection block");
        }
        TurnDegreesBlock block = (TurnDegreesBlock) root.getNext();

        assertEquals("`jEk@4|i[#Fk?(8x)AV.-my variable", block.getInputVariableID());
    }
}