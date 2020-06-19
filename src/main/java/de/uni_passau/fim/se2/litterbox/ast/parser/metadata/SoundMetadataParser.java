package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.SoundMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class SoundMetadataParser {
    public static SoundMetadata parse(JsonNode soundNode) {
        String assetId = null;
        if (soundNode.has(ASSET_ID_KEY)) {
            assetId = soundNode.get(ASSET_ID_KEY).asText();
        }

        String name = null;
        if (soundNode.has(NAME_KEY)) {
            name = soundNode.get(NAME_KEY).asText();
        }

        String md5ext = null;
        if (soundNode.has(MD5EXT_KEY)) {
            md5ext = soundNode.get(MD5EXT_KEY).asText();
        }

        String dataFormat = null;
        if (soundNode.has(DATA_FORMAT_KEY)) {
            dataFormat = soundNode.get(DATA_FORMAT_KEY).asText();
        }
        int rate = soundNode.get(Constants.RATE_KEY).asInt();
        int sampleCount = soundNode.get(SAMPLE_COUNT_KEY).asInt();
        return new SoundMetadata(assetId, name, md5ext, dataFormat, rate, sampleCount);
    }
}
