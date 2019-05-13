package utils.deserializer.scratch3;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.Costume;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CostumeDeserializer3 {

    /**
     * Deserialize the JSON String and creating a List<Costume> with Costume objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<Costume> with Costume objects
     */
    static List<Costume> deserialize(JsonNode rootNode) {
        JsonNode globalCostumes = rootNode.path("costumes");
        Iterator<JsonNode> elements = globalCostumes.elements();
        List<Costume> costumes = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode costume = elements.next();
            Costume scCostume = new Costume();
            scCostume.setName(costume.get("name").asText());
            scCostume.setAssetId(costume.get("assetId").asText());
            scCostume.setDataFormat(costume.get("dataFormat").asText());
            costumes.add(scCostume);
        }
        return costumes;
    }
}
