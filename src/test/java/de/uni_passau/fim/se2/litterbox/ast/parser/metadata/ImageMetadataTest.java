/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ImageMetadataTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/emptyProject.json");
        ImageMetadataList meta = program.getActorDefinitionList().getDefinitions().get(0).getActorMetadata().getCostumes();

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
