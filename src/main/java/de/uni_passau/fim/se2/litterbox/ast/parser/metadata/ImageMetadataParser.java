package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.ImageMetadata;

public class ImageMetadataParser {

    public static ImageMetadata parse(JsonNode imageNode) {
        String assetId = imageNode.get("assetId").asText();
        String name = imageNode.get("name").asText();
        String md5ext = imageNode.get("md5ext").asText();
        String dataFormat = imageNode.get("dataFormat").asText();
        Double bitmapResolution = null;
        if (imageNode.has("bitmapResolution")) {
            bitmapResolution = imageNode.get("bitmapResolution").asDouble();
        }
        double rotationCenterX = imageNode.get("rotationCenterX").asDouble();
        double rotationCenterY = imageNode.get("rotationCenterY").asDouble();
        return new ImageMetadata(assetId, name, md5ext, dataFormat, bitmapResolution, rotationCenterX, rotationCenterY);
    }
}
