package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.BroadcastMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class BroadcastMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;
    private static JsonNode empty;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = mapper.readTree(f);
        f = new File("./src/test/fixtures/bugpattern/broadcastSync.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testEmptyProgram() {
        BroadcastMetadataList monitors = BroadcastMetadataListParser.parse(empty.get("targets").get(0)
                .get("broadcasts"));
        Assertions.assertEquals(0, monitors.getList().size());
    }

    @Test
    public void testBroadcastsProgram() {
        BroadcastMetadataList monitors = BroadcastMetadataListParser.parse(prog.get("targets").get(0)
                .get("broadcasts"));
        Assertions.assertEquals(5, monitors.getList().size());
        Assertions.assertEquals("0kl{~LnXg|/EA][m;V;9", monitors.getList().get(0).getBroadcastID());
        Assertions.assertEquals("received", monitors.getList().get(0).getBroadcastName());
    }
}
