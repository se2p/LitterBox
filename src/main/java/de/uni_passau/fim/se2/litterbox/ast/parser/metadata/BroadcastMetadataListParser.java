package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.BroadcastMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.BroadcastMetadataList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BroadcastMetadataListParser {

    public static BroadcastMetadataList parse(JsonNode broadcastsNode){
        List<BroadcastMetadata> broadcastMetadataList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> entries = broadcastsNode.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> current = entries.next();
            broadcastMetadataList.add(new BroadcastMetadata(current.getKey(), current.getValue().asText()));
        }
        return new BroadcastMetadataList(broadcastMetadataList);
    }
}
