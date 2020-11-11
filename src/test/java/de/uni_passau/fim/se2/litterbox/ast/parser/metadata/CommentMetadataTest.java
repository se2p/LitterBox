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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.CommentMetadataList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.COMMENTS_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.TARGETS_KEY;

public class CommentMetadataTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEmptyProgram() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        JsonNode empty = mapper.readTree(f);
        CommentMetadataList monitors = CommentMetadataListParser.parse(empty.get(TARGETS_KEY).get(0)
                .get(COMMENTS_KEY));
        Assertions.assertEquals(0, monitors.getList().size());
    }

    @Test
    public void testCommentsProgram() throws IOException {
        File f = new File("./src/test/fixtures/metadata/commentMetadata.json");
        JsonNode prog = mapper.readTree(f);
        CommentMetadataList monitors = CommentMetadataListParser.parse(prog.get(TARGETS_KEY).get(1)
                .get(COMMENTS_KEY));
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
