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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.SpriteMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.TARGETS_KEY;

public class ActorMetadataTest {

    @Test
    public void testActorMetadata() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File f = new File("./src/test/fixtures/emptyProject.json");
        JsonNode empty = mapper.readTree(f);
        ActorMetadata actor0 = ActorMetadataParser.parse(empty.get(TARGETS_KEY).get(0));
        Assertions.assertTrue(actor0 instanceof StageMetadata);
        StageMetadata stage = (StageMetadata) actor0;
        ActorMetadata actor1 = ActorMetadataParser.parse(empty.get(TARGETS_KEY).get(1));
        Assertions.assertTrue(actor1 instanceof SpriteMetadata);
        SpriteMetadata sprite = (SpriteMetadata) actor1;

        Assertions.assertNotNull(stage.getCostumes());
        Assertions.assertNotNull(stage.getSounds());
        Assertions.assertNotNull(stage.getCommentsMetadata());
        Assertions.assertNotNull(stage.getBroadcasts());
        Assertions.assertNotNull(stage.getVariables());
        Assertions.assertNotNull(stage.getLists());
        Assertions.assertNull(stage.getTextToSpeechLanguage());
        Assertions.assertEquals("on", stage.getVideoState());
        Assertions.assertEquals(0, stage.getCurrentCostume());
        Assertions.assertEquals(60, stage.getTempo());
        Assertions.assertEquals(50, stage.getVideoTransparency());
        Assertions.assertEquals(0, stage.getLayerOrder());
        Assertions.assertEquals(100, stage.getVolume());

        Assertions.assertTrue(sprite.isVisible());
        Assertions.assertFalse(sprite.isDraggable());
        Assertions.assertEquals("all around", sprite.getRotationStyle());
        Assertions.assertEquals(0, sprite.getX());
        Assertions.assertEquals(0, sprite.getY());
        Assertions.assertEquals(100, sprite.getSize());
        Assertions.assertEquals(90, sprite.getDirection());
        Assertions.assertEquals(1, sprite.getLayerOrder());
    }
}

