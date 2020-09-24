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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ImageMetadataParser {

    public static ImageMetadata parse(JsonNode imageNode) {

        String assetId = null;
        if (imageNode.has(ASSET_ID_KEY)) {
            assetId = imageNode.get(ASSET_ID_KEY).asText();
        }

        String name = null;
        if (imageNode.has(NAME_KEY)) {
            name = imageNode.get(NAME_KEY).asText();
        }

        String md5ext = null;
        if (imageNode.has(MD5EXT_KEY)) {
            md5ext = imageNode.get(MD5EXT_KEY).asText();
        }

        String dataFormat = null;
        if (imageNode.has(DATA_FORMAT_KEY)) {
            dataFormat = imageNode.get(DATA_FORMAT_KEY).asText();
        }

        Double bitmapResolution = null;
        if (imageNode.has(BITMAP_KEY)) {
            bitmapResolution = imageNode.get(BITMAP_KEY).asDouble();
        }
        double rotationCenterX = imageNode.get(ROTATIONX_KEY).asDouble();
        double rotationCenterY = imageNode.get(ROTATIONY_KEY).asDouble();

        return new ImageMetadata(assetId, name, md5ext, dataFormat, bitmapResolution, rotationCenterX, rotationCenterY);
    }
}
