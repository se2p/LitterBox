package scratch.newast.parser;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.Program;
import scratch.newast.visitor.DotVisitor;

/**
 * This class contains test cases for a program that contains most constructions from the AST. The fixture for these
 * tests contains at least one Expression of each type and various statements.
 */
public class CombinedProgramTest {

    private JsonNode project;

    @Before
    public void setup() {
        String path = "src/test/java/scratch/fixtures/combinedProgram.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void dummyParseAllBlocks() {
        String path = "src/test/java/scratch/fixtures/allBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
            Program program = ProgramParser.parseProgram("All", project);
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testVisitor() {
        DotVisitor visitor = new DotVisitor();
        String path = "src/test/java/scratch/fixtures/allBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
            Program program = ProgramParser.parseProgram("All", project);
            program.accept(visitor);
            visitor.printGraph();
            //visitor.saveGraph("./target/graph.dot");
        } catch (IOException | ParsingException e) {
            fail();
        }
    }
}