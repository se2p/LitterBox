package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ExtensionMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MonitorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MonitorSliderMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class MonitorMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;
    private static JsonNode empty;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty =  mapper.readTree(f);
        f = new File("./src/test/fixtures/metadata/monitorMeta.json");
        prog =  mapper.readTree(f);
    }

    @Test
    public void testEmptyProgram() {
        MonitorMetadataList monitors = MonitorMetadataListParser.parse(empty.get("monitors"));
        Assertions.assertEquals(2, monitors.getList().size());
    }

    @Test
    public void testMonitorsProgram() {
        MonitorMetadataList monitors = MonitorMetadataListParser.parse(prog.get("monitors"));
        Assertions.assertEquals(4, monitors.getList().size());
        Assertions.assertTrue(monitors.getList().get(0) instanceof MonitorListMetadata);
        Assertions.assertTrue(monitors.getList().get(1) instanceof MonitorSliderMetadata);
        Assertions.assertTrue(monitors.getList().get(2) instanceof MonitorSliderMetadata);
        Assertions.assertTrue(monitors.getList().get(3) instanceof MonitorSliderMetadata);

    }
}
