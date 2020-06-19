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
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNode empty;

    @BeforeAll
    public static void setUp() throws IOException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = mapper.readTree(f);
    }

    @Test
    public void testActorMetadata() {
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
        Assertions.assertEquals(0,stage.getCurrentCostume());
        Assertions.assertEquals(60,stage.getTempo());
        Assertions.assertEquals(50,stage.getVideoTransparency());
        Assertions.assertEquals(0,stage.getLayerOrder());
        Assertions.assertEquals(100,stage.getVolume());

        Assertions.assertTrue(sprite.isVisible());
        Assertions.assertFalse(sprite.isDraggable());
        Assertions.assertEquals("all around", sprite.getRotationStyle());
        Assertions.assertEquals(0, sprite.getX());
        Assertions.assertEquals(0, sprite.getY());
        Assertions.assertEquals(100, sprite.getSize());
        Assertions.assertEquals(90, sprite.getDirection());
        Assertions.assertEquals(1,sprite.getLayerOrder());
    }
}

