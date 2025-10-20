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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentMetadataTest implements JsonTest {

    @Test
    void testEmptyProgram() throws Exception {
        final Program program = getAST("src/test/fixtures/emptyProject.json");
        assertAll(program.getActorDefinitionList().getDefinitions().stream().map(actorDefinition -> () -> {
            assertTrue(actorDefinition.getActorMetadata().getCommentsMetadata().getList().isEmpty());
        }));
    }

    @Test
    void testCommentsProgram() throws Exception {
        final Program program = getAST("src/test/fixtures/metadata/commentMetadata.json");
        final CommentMetadataList comments = program.getActorDefinitionList().getDefinitions().stream()
                                .filter(actor -> "Figur1".equals(actor.getIdent().getName()))
                                .findFirst()
                                .orElseThrow()
                                .getActorMetadata().getCommentsMetadata();

        assertEquals(2, comments.getList().size());
        assertEquals("csOuoew*W[q[5bX=8_ah", comments.getList().getFirst().getCommentId());
        assertEquals("z;/~(2Z_Xv9~^9yakTU1", comments.getList().getFirst().getBlockId());
        assertEquals("Block comment", comments.getList().getFirst().getText());
        assertFalse(comments.getList().getFirst().isMinimized());
        assertEquals(200, comments.getList().getFirst().getHeight());
        assertEquals(200, comments.getList().getFirst().getWidth());
        assertEquals(504.0740740740741, comments.getList().getFirst().getX());
        assertEquals(457.48148148148147, comments.getList().getFirst().getY());
    }
}
