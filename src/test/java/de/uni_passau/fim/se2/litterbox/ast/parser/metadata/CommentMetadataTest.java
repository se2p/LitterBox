package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.ListMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommentMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;
    private static JsonNode empty;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = mapper.readTree(f);
        f = new File("./src/test/fixtures/metadata/commentMetadata.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testEmptyProgram() {
        CommentMetadataList monitors = CommentMetadataListParser.parse(empty.get("targets").get(0)
                .get("comments"));
        Assertions.assertEquals(0, monitors.getList().size());
    }

    @Test
    public void testCommentsProgram() {
        CommentMetadataList monitors = CommentMetadataListParser.parse(prog.get("targets").get(1)
                .get("comments"));
        Assertions.assertEquals(2, monitors.getList().size());
        Assertions.assertEquals("csOuoew*W[q[5bX=8_ah", monitors.getList().get(0).getCommentId());
        Assertions.assertEquals("z;/~(2Z_Xv9~^9yakTU1", monitors.getList().get(0).getBlockId());
        Assertions.assertEquals("Block comment", monitors.getList().get(0).getText());
        Assertions.assertFalse(monitors.getList().get(0).isMinimized());
        Assertions.assertEquals(200, monitors.getList().get(0).getHeight());
        Assertions.assertEquals(200, monitors.getList().get(0).getWidth());
        Assertions.assertEquals(504.0740740740741, monitors.getList().get(0).getX());
        Assertions.assertEquals(457.48148148148147, monitors.getList().get(0).getY());
    }
}
