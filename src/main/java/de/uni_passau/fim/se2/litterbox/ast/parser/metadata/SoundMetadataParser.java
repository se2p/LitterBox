package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.SoundMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.NAME_KEY;

public class SoundMetadataParser {
    public static SoundMetadata parse(JsonNode soundNode) {
        String assetId = soundNode.get("assetId").asText();
        String name = soundNode.get(NAME_KEY).asText();
        String md5ext = soundNode.get("md5ext").asText();
        String dataFormat = soundNode.get("dataFormat").asText();
        int rate = soundNode.get("rate").asInt();
        int sampleCount = soundNode.get("sampleCount").asInt();
        return new SoundMetadata(assetId, name, md5ext, dataFormat, rate, sampleCount);
    }
}
