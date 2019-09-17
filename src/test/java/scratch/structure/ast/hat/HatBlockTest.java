package scratch.structure.ast.hat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.stack.MoveStepBlock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.junit.Assert.*;

public class HatBlockTest {

    JsonNode script;

    @Before
    public void setup() {

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        ObjectMapper mapper = new ObjectMapper();
        try {

            BufferedReader br = new BufferedReader(new FileReader("./src/test/java/scratch/structure/ast/fixtures/whenflagclicked.json"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);        //.append('\n');
            }
            JsonNode rootNode = mapper.readTree(sb.toString());

            Iterator<JsonNode> elements = rootNode.get("targets").elements();
            while (elements.hasNext()) {
                JsonNode c = elements.next();
                if (c.has("isStage") && !c.get("isStage").asBoolean() && c.has("blocks")) {
                    script = c.get("blocks");
                    break;
                }
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void readMoveStepsScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof WhenFlagClickedBlock)) {
            fail("Result of this fixture should be a movesteps block");
        }

        BasicBlock node = root;
        int count = 1;
        while(node.getNext() != null) {
            count++;
            node = node.getNext();
        }

        WhenFlagClickedBlock block = (WhenFlagClickedBlock) root;
        assertEquals("One node expected", 1, count);
        assertEquals("event_whenflagclicked", block.getOpcode());
    }
}