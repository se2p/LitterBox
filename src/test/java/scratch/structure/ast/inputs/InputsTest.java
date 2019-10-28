package scratch.structure.ast.inputs;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.*;
import scratch.structure.ast.bool.BooleanBlock;
import scratch.structure.ast.bool.OperatorNot;
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
        assertEquals("\"STEPS\": [3, [12, meine Variable, `jEk@4|i[#Fk?(8x)AV.-my variable], [4, 20]", ((MoveStepBlock) next).getSlot().toString());
    }

    @Test
    public void testList() {
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        ScriptBodyBlock next = (ScriptBodyBlock) ((Extendable) root).getNext();
        next = (ScriptBodyBlock) next.getNext();
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
        assertEquals("\"STEPS\": [3, xHN|Q?;cVFW*Xe%rSOmU, [4, 40]", ((MoveStepBlock) next).getSlot().toString());
    }

    @Test
   public void testBoolean() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/operators.json");
        Ast ast = new Ast();
        ast.parseScript(script);
        ScratchBlock root = ast.getRoot();
        BooleanBlock next = (BooleanBlock) root;
        assertEquals("\"OPERAND\": [2, sO~t38~=J!nqZ(qm/{v!]", ((OperatorNot) next).getCondition().toString());

        ScratchBlock operatorGT = (ScratchBlock) ((OperatorNot) next).getCondition().getPrimary();
        assertEquals("\"OPERAND1\": [1, [10, 30]],\"OPERAND2\": [1, [10, 50]], [0,0]", operatorGT.toString());
    }
}
