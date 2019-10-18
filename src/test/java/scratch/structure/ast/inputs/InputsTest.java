package scratch.structure.ast.inputs;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.stack.MoveStepBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;

public class InputsTest {
    JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/slottest.json");
    }

    @Test
    public void testLiteral() {
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        assertEquals("\"STEPS\": [1, [4, 10]]", ((MoveStepBlock) root).getSlot().toString());
    }

    @Test
    public void testVariable() {
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        ScratchBlock next = (ScratchBlock) root.getNext();
        // This is supposed to fail as soon as the variable block is implemented so that the test is updated
        assertEquals("\"STEPS\": [3, [null], [4, 20]", ((MoveStepBlock) next).getSlot().toString());
    }

    @Test
    public void testList() {
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        ScratchBlock next = (ScratchBlock) root.getNext();
        next = (ScratchBlock) next.getNext();
        // This is supposed to fail as soon as the list block is implemented so that the test is updated
        assertEquals("\"STEPS\": [3, [null], [4, 30]", ((MoveStepBlock) next).getSlot().toString());
    }

    @Test
    public void testReporter() {
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        ScratchBlock next = (ScratchBlock) root.getNext();
        next = (ScratchBlock) next.getNext();
        next = (ScratchBlock) next.getNext();
        // This is supposed to fail as soon as the list block is implemented so that the test is updated
        assertEquals("\"STEPS\": [3, [null], [4, 40]", ((MoveStepBlock) next).getSlot().toString());
    }

    @Test
    public void testBoolean() {
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        ScratchBlock next = (ScratchBlock) root.getNext();
        next = (ScratchBlock) next.getNext();
        next = (ScratchBlock) next.getNext();
        next = (ScratchBlock) next.getNext();
        // This is supposed to fail as soon as the list block is implemented so that the test is updated
        assertEquals("\"STEPS\": [3, [null], [4, 50]", ((MoveStepBlock) next).getSlot().toString());
    }
}
