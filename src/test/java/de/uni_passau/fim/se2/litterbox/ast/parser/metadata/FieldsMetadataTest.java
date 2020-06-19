package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.FieldsMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class FieldsMetadataTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode field;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/metadata/fieldsMeta.json");
        field = mapper.readTree(f);
    }

    @Test
    public void testReferenceField(){
        FieldsMetadataList fieldsMetadataList =
                FieldsMetadataListParser.parse(field.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                        "VGF5,Fcjj=a8}+G|jcE:").get(FIELDS_KEY));

        Assertions.assertEquals(VARIABLE_KEY,fieldsMetadataList.getList().get(0).getFieldsName());
        Assertions.assertEquals("`jEk@4|i[#Fk?(8x)AV.-my variable",fieldsMetadataList.getList().get(0).getFieldsReference());
        Assertions.assertEquals("my variable",fieldsMetadataList.getList().get(0).getFieldsValue());
    }

    @Test
    public void testValueField(){
        FieldsMetadataList fieldsMetadataList =
                FieldsMetadataListParser.parse(field.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                        "OLSBG!s$*uJ(;~BQ2qi{").get(FIELDS_KEY));

        Assertions.assertEquals("TO",fieldsMetadataList.getList().get(0).getFieldsName());
        Assertions.assertNull(
                fieldsMetadataList.getList().get(0).getFieldsReference());
        Assertions.assertEquals("_random_",fieldsMetadataList.getList().get(0).getFieldsValue());
    }
}
