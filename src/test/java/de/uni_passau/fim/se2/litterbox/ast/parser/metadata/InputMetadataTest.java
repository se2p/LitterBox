/*
 * Copyright (C) 2020 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.DataInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.InputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.ReferenceInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.TypeInputMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class InputMetadataTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode prog;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/metadata/blockMeta.json");
        prog = mapper.readTree(f);
    }

    @Test
    public void testTypeInput() {
        InputMetadataList inputsMetadata =
                InputMetadataListParser.parse(prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                        "$C@+K-:6ie`W)?I*4jc9").get(INPUTS_KEY));
        InputMetadata input = inputsMetadata.getList().get(0);
        Assertions.assertTrue(input instanceof TypeInputMetadata);
        TypeInputMetadata typeInput = (TypeInputMetadata) input;
        Assertions.assertEquals(10, typeInput.getType());
        Assertions.assertEquals("Hello!", typeInput.getValue());
    }

    @Test
    public void testReferenceInput() {
        InputMetadataList inputsMetadata =
                InputMetadataListParser.parse(prog.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                        "Vr$zTl8mo1W,U?+q6,T{").get(INPUTS_KEY));
        InputMetadata input = inputsMetadata.getList().get(0);
        Assertions.assertTrue(input instanceof ReferenceInputMetadata);
        ReferenceInputMetadata reference = (ReferenceInputMetadata) input;
        Assertions.assertEquals("k~QZ.p5)uSGZZ]?@TWD$", reference.getInputName());
        Assertions.assertEquals("c@bcun.aPW8(fPX~fG]f", reference.getReference());
    }

    @Test
    public void testDataInput() throws IOException {
        File f = new File("./src/test/fixtures/metadata/fieldsMeta.json");
        JsonNode field = mapper.readTree(f);
        InputMetadataList inputsMetadata =
                InputMetadataListParser.parse(field.get(TARGETS_KEY).get(1).get(BLOCKS_KEY).get(
                        "W93h`Dsu0+Mnx_;OGLY8").get(INPUTS_KEY));
        InputMetadata input = inputsMetadata.getList().get(0);
        Assertions.assertTrue(input instanceof DataInputMetadata);
        DataInputMetadata reference = (DataInputMetadata) input;
        Assertions.assertEquals("TO", reference.getInputName());
        Assertions.assertEquals("my variable", reference.getDataName());
        Assertions.assertEquals("`jEk@4|i[#Fk?(8x)AV.-my variable", reference.getDataReference());
        Assertions.assertEquals(VAR_PRIMITIVE, reference.getDataType());
    }
}

