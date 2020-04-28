package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.MetaMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class MetaMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/metadata/metaExtensionMonitorData.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testMeta() {
        MetaMetadata meta = MetaMetadataParser.parse(prog.get("meta"));
        Assertions.assertEquals("0.2.0-prerelease.20200402182733", meta.getVm());
        Assertions.assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0",
                meta.getAgent());
        Assertions.assertEquals("3.0.0", meta.getSemver());
    }
}
