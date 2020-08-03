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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.ImageMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.COSTUMES_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.TARGETS_KEY;

public class ImageMetadataTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode empty;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = mapper.readTree(f);
    }

    @Test
    public void testEmptyProgram() {
        ImageMetadataList meta = ImageMetadataListParser.parse(empty.get(TARGETS_KEY).get(0)
                .get(COSTUMES_KEY));
        Assertions.assertEquals(1, meta.getList().size());
        Assertions.assertEquals("cd21514d0531fdffb22204e0ec5ed84a", meta.getList().get(0).getAssetId());
        Assertions.assertEquals("svg", meta.getList().get(0).getDataFormat());
        Assertions.assertEquals("cd21514d0531fdffb22204e0ec5ed84a.svg", meta.getList().get(0).getMd5ext());
        Assertions.assertEquals("backdrop1", meta.getList().get(0).getName());
        Assertions.assertNull(meta.getList().get(0).getBitmapResolution());
        Assertions.assertEquals(240, meta.getList().get(0).getRotationCenterX());
        Assertions.assertEquals(180, meta.getList().get(0).getRotationCenterY());
    }
}
