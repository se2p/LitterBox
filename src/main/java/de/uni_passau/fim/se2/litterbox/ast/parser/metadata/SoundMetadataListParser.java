package de.uni_passau.fim.se2.litterbox.ast.parser.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class SoundMetadataListParser {
    public static SoundMetadataList parse(JsonNode soundsNode) {
        List<SoundMetadata> soundMetadataList = new ArrayList<>();
        Preconditions.checkArgument(soundsNode instanceof ArrayNode);
        ArrayNode soundsArray = (ArrayNode) soundsNode;
        for (int i = 0; i < soundsArray.size(); i++) {
            soundMetadataList.add(SoundMetadataParser.parse(soundsArray.get(i)));
        }


        return new SoundMetadataList(soundMetadataList);
    }
}
