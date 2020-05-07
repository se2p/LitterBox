package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.ImageMetadata;

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
