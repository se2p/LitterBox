package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.ListMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;
    private static JsonNode empty;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = mapper.readTree(f);
        f = new File("./src/test/fixtures/metadata/monitorMeta.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testEmptyProgram() {
        ListMetadataList monitors = ListMetadataListParser.parse(empty.get("targets").get(0)
                .get("lists"));
        Assertions.assertEquals(0, monitors.getList().size());
    }

    @Test
    public void testListsProgram() {
        ListMetadataList monitors = ListMetadataListParser.parse(prog.get("targets").get(0)
                .get("lists"));
        Assertions.assertEquals(1, monitors.getList().size());
        Assertions.assertEquals("h|7mn11vxAw-d7dTt,_/", monitors.getList().get(0).getListId());
        Assertions.assertEquals("test", monitors.getList().get(0).getListName());
        List<String> list = new ArrayList<>();
        list.add("bla");
        list.add("test");
        Assertions.assertEquals(list, monitors.getList().get(0).getValues());
    }
}
