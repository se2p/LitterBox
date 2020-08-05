package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProgramMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.MONITORS_KEY;

public class ProgramMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/metadata/missingMonitorsNode.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testMissingMonitors() {
        Assertions.assertFalse(prog.has(MONITORS_KEY));
        ProgramMetadata metadata = ProgramMetadataParser.parse(prog);
        Assertions.assertEquals(0, metadata.getMonitor().getList().size());
        Assertions.assertEquals(0, metadata.getExtension().getExtensionNames().size());
    }
}
