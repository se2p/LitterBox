package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SetCoordinateToBlockTest {

    JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/setcoordinateto.json");
    }

    @Test
    public void testStructure() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof SetCoordinateToBlock)) {
            fail("Result of this fixture should be a setcoordinateto block");
        }

        BasicBlock node = root;
        int count = 0;
        while (node.getNext() != null) {
            count++;
            node = (BasicBlock) node.getNext();
        }
        assertEquals("Three nodes expected", 3, count);
    }

    @Test
    public void testIntInput() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof SetCoordinateToBlock)) {
            fail("Result of this fixture should be a setcoordinateto block");
        }
        SetCoordinateToBlock block = (SetCoordinateToBlock) root;
        assertEquals(10, block.getInputValue());
    }

    @Test
    public void testVariableInput() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof SetCoordinateToBlock)) {
            fail("Result of this fixture should be a setcoordinateto block");
        }
        SetCoordinateToBlock block = (SetCoordinateToBlock) ((SetCoordinateToBlock) (root.getNext())).getNext();

        assertEquals("`jEk@4|i[#Fk?(8x)AV.-my variable", block.getInputID());
    }

    @Test
    public void testListInput() {
        Ast ast = new Ast();
        ast.parseScript(JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/setcoordinateto_list.json"));

        BasicBlock root = ast.getRoot();
        if (!(root instanceof SetCoordinateToBlock)) {
            fail("Result of this fixture should be a setcoordinateto block");
        }
        SetCoordinateToBlock block = (SetCoordinateToBlock) root.getNext();

        assertEquals("TOXx2wvdeM)Hd4dXaT~-", block.getInputID());
    }

     @Test
    public void testXAndYDistinction() {
         Ast ast = new Ast();
         ast.parseScript(script);

         BasicBlock root = ast.getRoot();
         if (!(root instanceof SetXCoordinateToBlock)) {
             fail("Result of this fixture should be a setxcoordinateto block");
         }

         SetCoordinateToBlock block = (SetCoordinateToBlock) root.getNext();
         if (!(block instanceof SetYCoordinateToBlock)) {
             fail("Result of this fixture should be a setycoordinateto block");
         }

         block = (SetCoordinateToBlock) block.getNext();
         if (!(block instanceof SetXCoordinateToBlock)) {
             fail("Result of this fixture should be a setxcoordinateto block");
         }

         block = (SetCoordinateToBlock) block.getNext();
         if (!(block instanceof SetYCoordinateToBlock)) {
             fail("Result of this fixture should be a setycoordinateto block");
         }
     }
}

