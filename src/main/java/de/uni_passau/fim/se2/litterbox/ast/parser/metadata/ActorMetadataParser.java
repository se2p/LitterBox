package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.*;

public class ActorMetadataParser {

    public static ActorMetadata parse(JsonNode actorNode) {
        CommentMetadataList commentsMetadata;
        VariableMetadataList variables;
        ListMetadataList lists;
        BroadcastMetadataList broadcasts;
        int currentCostume;
        ImageMetadataList costumes;
        SoundMetadataList sounds;
        double volume;
        int layerOrder;
        return null;
    }
}
