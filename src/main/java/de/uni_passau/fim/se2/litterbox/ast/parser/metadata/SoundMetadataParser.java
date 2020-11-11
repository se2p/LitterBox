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
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;

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
