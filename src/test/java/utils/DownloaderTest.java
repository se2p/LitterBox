package utils;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;
import scratch.ast.visitor.DotVisitor;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DownloaderTest {

    private JsonNode project;

    @Test
    void downloadProjectJSON() {
        try {
            String json = Downloader.downloadProjectJSON("338832275");
            Downloader.saveDownloadedProject(json, "stuff", "/tmp/");

            ObjectMapper objectMapper = new ObjectMapper();
            project = objectMapper.readTree(json);
            Program program = ProgramParser.parseProgram("338832275", project);
            DotVisitor visitor = new DotVisitor();
            program.accept(visitor);
            //  visitor.printGraph();
            //  visitor.saveGraph("./target/graph.dot");
        } catch (IOException | ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testDownloaded() {
        String path = "/tmp/stuff.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
            Program parsed = ProgramParser.parseProgram("stuff", project);
        } catch (IOException | ParsingException e) {
            fail();
        }
    }
}