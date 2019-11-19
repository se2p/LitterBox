package utils;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;
import scratch.ast.visitor.DotVisitor;

class DownloaderTest {

    private JsonNode project;

    //  @Test
    void downloadProjectJSON() {
        try {
            String json = Downloader.downloadProjectJSON("338832275");
            ObjectMapper objectMapper = new ObjectMapper();
            project = objectMapper.readTree(json);
            Program program = ProgramParser.parseProgram("338832275", project);
            DotVisitor visitor = new DotVisitor();
            program.accept(visitor);
            visitor.printGraph();
            visitor.saveGraph("./target/graph.dot");
        } catch (IOException | ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}