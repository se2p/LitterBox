package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.Costume;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * More information about the JSON Scratch 2 file format and its JSON arrays and nodes:
 * https://en.scratch-wiki.info/wiki/Scratch_File_Format
 */
class CostumeDeserializer {

    /**
     * Deserialize the JSON String and creating a List<Costume> with Costume objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Costume> with Costume objects
     */
    static List<Costume> deserialize(JsonNode rootNode) {
        JsonNode globalVariables = rootNode.path("costumes");
        Iterator<JsonNode> elements = globalVariables.elements();
        List<Costume> costumes = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode variable = elements.next();
            Costume scCostume = new Costume();
            scCostume.setName(variable.get("costumeName").asText());
            scCostume.setAssetId(variable.get("baseLayerID").asText());
            costumes.add(scCostume);
        }
        return costumes;
    }
}
