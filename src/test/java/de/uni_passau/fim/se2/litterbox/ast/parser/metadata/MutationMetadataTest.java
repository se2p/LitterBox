package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CallMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.PrototypeMutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class MutationMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/metadata/blockMeta.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testProtoMutation(){
        MutationMetadata mutationMetadata = MutationMetadataParser.parse(prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                "Vr$zTl8mo1W,U?+q6,T{").get(MUTATION_KEY));
        Assertions.assertTrue(mutationMetadata instanceof PrototypeMutationMetadata);
        PrototypeMutationMetadata existing = (PrototypeMutationMetadata) mutationMetadata;
        Assertions.assertFalse(existing.isWarp());
        Assertions.assertEquals(0, existing.getChild().size());
        Assertions.assertEquals("mutation",existing.getTagName());
        Assertions.assertEquals("TestMethod %s",existing.getProcCode());
        Assertions.assertEquals("[\"k~QZ.p5)uSGZZ]?@TWD$\"]",existing.getArgumentIds());
        Assertions.assertEquals("[\"number or text\"]",existing.getArgumentNames());
        Assertions.assertEquals("[\"\"]",existing.getArgumentDefaults());
    }

    @Test
    public void testCallMutation(){
        MutationMetadata mutationMetadata = MutationMetadataParser.parse(prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                "O3bG_[t(B3p}k0KF:.,|").get(MUTATION_KEY));
        Assertions.assertTrue(mutationMetadata instanceof CallMutationMetadata);
        CallMutationMetadata existing = (CallMutationMetadata) mutationMetadata;
        Assertions.assertFalse(existing.isWarp());
        Assertions.assertEquals(0, existing.getChild().size());
        Assertions.assertEquals("mutation",existing.getTagName());
        Assertions.assertEquals("TestMethod %s",existing.getProcCode());
        Assertions.assertEquals("[\"k~QZ.p5)uSGZZ]?@TWD$\"]",existing.getArgumentIds());
    }
}
