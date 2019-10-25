package scratch.structure.ast.inputs;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.*;
import scratch.structure.ast.stack.MoveStepBlock;
import utils.JsonParser;

import static junit.framework.TestCase.assertEquals;

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
        Stackable next = ((Extendable) root).getNext();
        // This is supposed to fail as soon as the variable block is implemented so that the test is updated
        assertEquals("\"STEPS\": [3, [12, meine Variable, `jEk@4|i[#Fk?(8x)AV.-my variable], [4, 20]", ((MoveStepBlock) next).getSlot().toString());
    }

    @Test
    public void testList() {
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        ScriptBodyBlock next = (ScriptBodyBlock) ((Extendable) root).getNext();
        next = (ScriptBodyBlock) next.getNext();
        // This is supposed to fail as soon as the list block is implemented so that the test is updated
        assertEquals("\"STEPS\": [3, [12, listenname, _(ea0Bs}$B3XjB9*{^mC], [4, 30]", ((MoveStepBlock) next).getSlot().toString());
    }

    @Test
    public void testReporter() {
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        ScriptBodyBlock next = (ScriptBodyBlock) ((Extendable) root).getNext();
        next = (ScriptBodyBlock) next.getNext();
        next = (ScriptBodyBlock) next.getNext();
        // This is supposed to fail as soon as the list block is implemented so that the test is updated
        assertEquals("\"STEPS\": [3, xHN|Q?;cVFW*Xe%rSOmU, [4, 40]", ((MoveStepBlock) next).getSlot().toString());
    }
//
//    @Test
//    public void testBoolean() {
//        Ast ast = new Ast();
//        ast.parseScript(script);
//        ScratchBlock root = ast.getRoot();
//        ScriptBodyBlock next = (ScriptBodyBlock) ((Extendable) root).getNext();
//        next = (ScriptBodyBlock) next.getNext();
//        next = (ScriptBodyBlock) next.getNext();
//        next = (ScriptBodyBlock) next.getNext();
//        // This is supposed to fail as soon as the list block is implemented so that the test is updated
//        assertEquals("\"STEPS\": [3, [null], [4, 50]", ((MoveStepBlock) next).getSlot().toString());
//    }
}
