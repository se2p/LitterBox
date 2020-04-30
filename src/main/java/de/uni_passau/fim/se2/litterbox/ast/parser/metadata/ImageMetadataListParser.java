package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;


public class ImageMetadataListParser {

    public static ImageMetadataList parse(JsonNode imagesNode) {
        List<ImageMetadata> imageMetadataList = new ArrayList<>();
        Preconditions.checkArgument(imagesNode instanceof ArrayNode);
        ArrayNode imagesArray = (ArrayNode) imagesNode;
        for (int i = 0; i < imagesNode.size(); i++) {
            imageMetadataList.add(ImageMetadataParser.parse(imagesArray.get(i)));
        }


        return new ImageMetadataList(imageMetadataList);
    }
}
