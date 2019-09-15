package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.Project;
import scratch.structure.Sprite;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import utils.Version;
import utils.ZipReader;
import utils.deserializer.scratch3.SpriteDeserializer3;
import utils.deserializer.scratch3.StageDeserializer3;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class MoveStepBlockTest {

    JsonNode script;

    @Before
    public void setup() {

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        ObjectMapper mapper = new ObjectMapper();
        try {

            BufferedReader br = new BufferedReader(new FileReader("./src/test/java/scratch/structure/ast/stack/fixtures/movesteps.json"));
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
        if (!(root instanceof MoveStepBlock)) {
            fail("Result of this fixture should be a movesteps block");
        }

        BasicBlock node = root;
        int count = 0;
        while(node.getNext() != null) {
            count++;
            node = node.getNext();
        }

        MoveStepBlock block = (MoveStepBlock) root;
        assertEquals("Three nodes expected", 3, count);
        assertEquals(10, block.getSteps());
    }
}