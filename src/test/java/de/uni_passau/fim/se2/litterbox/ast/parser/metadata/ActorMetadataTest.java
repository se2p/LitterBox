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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActorMetadataTest {

    private final Scratch3Parser parser = new Scratch3Parser();

    @Test
    public void testActorMetadata() throws ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        Program program = parser.parseFile(f);

        ActorDefinition stage = program.getActorDefinitionList().getDefinitions().get(0);
        assertTrue(stage.isStage());
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().get(1);
        Assertions.assertFalse(actor.isStage());

        StageMetadata stageMeta = (StageMetadata) stage.getActorMetadata();
        assertNotNull(stageMeta.getCostumes());
        assertNotNull(stageMeta.getSounds());
        assertNotNull(stageMeta.getCommentsMetadata());
        assertNull(stageMeta.getTextToSpeechLanguage());
        assertEquals(0, stageMeta.getCurrentCostume());

        ActorMetadata actorMeta = actor.getActorMetadata();
        assertNotNull(actorMeta.getCostumes());
        assertNotNull(actorMeta.getSounds());
        assertNotNull(actorMeta.getCommentsMetadata());
        assertEquals(0, actorMeta.getCurrentCostume());
    }
}

