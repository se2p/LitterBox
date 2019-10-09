package scratch.structure.ast.cap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Utils;
import scratch.structure.ast.hat.WhenFlagClickedBlock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.junit.Assert.*;

public class DeleteCloneBlockTest {
    private JsonNode script;

    @Before
    public void setup() {
        script = Utils.parseScript("./src/test/java/scratch/structure/ast/fixtures/deleteclone.json");
    }

    @Test
    public void readDeleteCloneScript() {
        Ast ast = new Ast();
        ast.parseScript(script);

        BasicBlock root = ast.getRoot();
        if (!(root instanceof DeleteCloneBlock)) {
            fail("Result of this fixture should be a deleteClone block");
        }

        BasicBlock node = root;
        int count = 1;
        while(node.getNext() != null) {
            count++;
            node = (BasicBlock) node.getNext();
        }

        DeleteCloneBlock block = (DeleteCloneBlock) root;
        assertEquals("One node expected", 1, count);
        assertEquals("control_delete_this_clone", block.getOpcode());
    }
}

