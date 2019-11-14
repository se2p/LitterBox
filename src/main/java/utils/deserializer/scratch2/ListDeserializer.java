package utils.deserializer.scratch2;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import scratch.data.ScList;

/**
 * More information about the JSON Scratch 2 file format and its JSON arrays and nodes:
 * https://en.scratch-wiki.info/wiki/Scratch_File_Format
 */
class ListDeserializer {

    /**
     * Deserialize the JSON String and creating a List<ScList> with ScList objects
     *
     * @param rootNode the JsonNode to deserialize
     * @return a List<ScList> with ScList objects
     */
    static List<ScList> deserialize(JsonNode rootNode) {
        JsonNode globalLists = rootNode.path("lists");
        Iterator<JsonNode> elements = globalLists.elements();
        List<ScList> lists = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode variable = elements.next();
            ScList scList = new ScList();
            scList.setName(variable.get("listName").asText());
            scList.setContent(new ArrayList<>());
            JsonNode content = globalLists.path("contents");
            if (content.isArray()) {
                for (final JsonNode objNode : content) {
                    scList.getContent().add(objNode.asText());
                }
            }
            int[] pos = {globalLists.path("x").asInt(), globalLists.path("y").asInt()};
            scList.setPosition(pos);
            scList.setVisible(globalLists.path("visible").asBoolean());
            lists.add(scList);
        }
        return lists;
    }

}
